package club.staircrusher.place

import club.staircrusher.readTsvAsLines
import club.staircrusher.TargetRegionInfo
import club.staircrusher.infra.network.RateLimiterFactory
import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.infra.adapter.out.web.KakaoMapsService
import club.staircrusher.place.infra.adapter.out.web.KakaoProperties
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File

private const val kakaoApiKey = "fake key"

private data class CategoryGroup(
    val name: String,
    val code: String,
) {
    override fun toString(): String {
        return "$name($code)"
    }
}

private data class TargetRegionPlaceCount(
    val targetRegionInfo: TargetRegionInfo,
    val radiusMeters: Int,
    val count: Map<CategoryGroup, Int>,
) {
    fun toCsvRow(): String {
        return "${targetRegionInfo.name}(${radiusMeters}m)," + count.toList().joinToString(",") { it.second.toString() }
    }
}

private val targetCategoryGroups = listOf(
    CategoryGroup(name = "음식점", code = "FD6"),
    CategoryGroup(name = "카페", code = "CE7"),
    CategoryGroup(name = "약국", code = "PM9"),
    CategoryGroup(name = "병원", code = "HP8"),
    CategoryGroup(name = "편의점", code = "CS2"),
)

@Suppress("MagicNumber")
private val radiusMetersList = listOf(500)

private val kakaoMapsService = KakaoMapsService(KakaoProperties(kakaoApiKey), RateLimiterFactory(SimpleMeterRegistry()))

fun main() {
    val regionInfos = getTargetRegionInfos()
    val rows = getTargetRegionPlaceCounts(regionInfos).map { it.toCsvRow() }

    val headerRow = "역 이름," + targetCategoryGroups.joinToString(",")
    println(listOf(headerRow) + rows)

    File("result.csv").writeText(buildString {
        appendLine(headerRow)
        rows.forEach { appendLine(it) }
    })
}

private fun getTargetRegionInfos(): List<TargetRegionInfo> {
    val targetStationInfos = readTsvAsLines("place_count/target_station_infos.tsv")

    data class Station(
        val subwayLine: String,
        val name: String,
    ) {
        val fullname: String
            get() = "$name $subwayLine"
    }

    val stations = targetStationInfos.map { (rawSubwayLine, rawStationName) ->
        val normalizedStationName = rawStationName
            .replace(Regex("\\(.*\\)"), "")
            .let {
                if (!it.endsWith("역")) {
                    it + "역"
                } else {
                    it
                }
            }
        val normalizedSubwayLine = rawSubwayLine.replace(Regex("호선.*"), "호선")
        Station(subwayLine = normalizedSubwayLine, name = normalizedStationName)
    }
    println(stations)
    val hardCodedTargetRegionInfosByPlaceName = listOf(
        "충무로역 3호선" to TargetRegionInfo(
            name = "충무로역 3호선",
            lng = 126.994728,
            lat = 37.560997,
        )
    ).toMap()
    val targetRegionInfos = runBlocking {
        val deferredList = stations.map { station ->
            async {
                hardCodedTargetRegionInfosByPlaceName[station.fullname]?.let {
                    println("station: ${it.name} / ${it.name}")
                    return@async it
                }
                val place = kakaoMapsService.findFirstByKeyword(station.fullname, MapsService.SearchByKeywordOption())
                    ?: kakaoMapsService.findFirstByKeyword(station.name, MapsService.SearchByKeywordOption())
                    ?: throw IllegalArgumentException("${station.fullname}에 해당하는 장소가 없습니다.")
                println("station: ${station.fullname} / ${place.name}")
                TargetRegionInfo(
                    name = station.fullname,
                    lng = place.location.lng,
                    lat = place.location.lat,
                )
            }
        }
        awaitAll(*deferredList.toTypedArray())
    }
    return targetRegionInfos
}

@Suppress("MagicNumber")
private fun getTargetRegionPlaceCounts(regionInfos: List<TargetRegionInfo>): List<TargetRegionPlaceCount> {
    return regionInfos.flatMap { regionInfo ->
        radiusMetersList.map { radiusMeters ->
            val placeCountByCategoryGroup = targetCategoryGroups.map { categoryGroup ->
                val (_, _, result) = "https://dapi.kakao.com/v2/local/search/category.json"
                    .httpGet(listOf(
                        "category_group_code" to categoryGroup.code,
                        "x" to regionInfo.lng,
                        "y" to regionInfo.lat,
                        "radius" to radiusMeters,
                        "size" to 15,
                        "page" to 1,
                    ))
                    .header(
                        "Authorization" to "KakaoAK $kakaoApiKey",
                    )
                    .responseString()

                categoryGroup to when (result) {
                    is Result.Failure -> {
                        println(result.getException())
                        "-1"
                    }

                    is Result.Success -> {
                        Regex("\"total_count\":\\s*(\\d*)").find(result.value)?.groups?.get(1)?.value ?: "-1"
                    }
                }.toInt()
            }.toMap()
            radiusMeters to placeCountByCategoryGroup
        }
            .map { (radiusMeters, placeCountByCategoryGroup) -> TargetRegionPlaceCount(regionInfo, radiusMeters, placeCountByCategoryGroup) }
    }
}
