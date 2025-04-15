package club.staircrusher.place

import club.staircrusher.TargetRegionInfo
import club.staircrusher.infra.network.RateLimiterFactory
import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.place.infra.adapter.out.web.KakaoMapsService
import club.staircrusher.place.infra.adapter.out.web.KakaoProperties
import club.staircrusher.readTsvAsLines
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File

private const val kakaoApiKey = "fake key"

private data class SearchPlacesResult(
    val targetRegionInfo: TargetRegionInfo,
    val radiusMeters: Int,
    val places: Map<PlaceCategory, List<Place>>,
    val totalNumber: Map<PlaceCategory, Int>,
)

private val targetCategories = listOf(
    PlaceCategory.RESTAURANT,
    PlaceCategory.CAFE,
    PlaceCategory.PHARMACY,
    PlaceCategory.HOSPITAL,
    PlaceCategory.CONVENIENCE_STORE,
)

private fun PlaceCategory.toCsvFormat() = this.humanReadableName + "(${KakaoMapsService.SearchResult.Document.Category.fromPlaceCategory(this)})"
private val headerRow = "지하철역,building_id,place_id,이름,카테고리,위도,경도,주소"
private fun Place.toCsvRow(): String = "${building.id},${id},$name,${category?.toCsvFormat()},${location.lat},${location.lng},$address"

@Suppress("MagicNumber")
private val radiusMeter: Int = 500

private val kakaoMapsService = KakaoMapsService(KakaoProperties(kakaoApiKey), RateLimiterFactory(SimpleMeterRegistry()))

fun main() = runBlocking {
    val regionInfos = getTargetRegionInfos()
    val placesByRegionInfo = regionInfos.map { regionInfo ->
        val searchPlacesResult = fetchAllSearchPlaces(regionInfo, radiusMeter)
        regionInfo to searchPlacesResult.places.values.flatten().sortedBy { it.category }
    }
    File("지하철역_근처_장소_검색결과.csv").writeText(buildString {
        appendLine(headerRow)
        placesByRegionInfo.forEach { (regionInfo, places) ->
            places.forEach { place ->
                appendLine(listOf(regionInfo.name, place.toCsvRow()).joinToString(","))
            }
        }
    })
}

/**
 * 카카오 지도를 이용해서 표준 지하철 역 이름 구하기 (ex. "압구정로데오역 분당선" -> "압구정로데오역 수인분당선")
 */
private fun getTargetRegionInfos(): List<TargetRegionInfo> {
    data class Station(
        val subwayLine: String,
        val name: String,
    ) {
        val fullname: String
            get() = "$name $subwayLine"
    }

    val targetStationInfos = readTsvAsLines("place_count/target_station_infos.tsv")
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
    val targetRegionInfos = runBlocking {
        val deferredList = stations.map { station ->
            async {
                val place = kakaoMapsService.findFirstByKeyword(station.fullname, MapsService.SearchByKeywordOption())
                    ?: kakaoMapsService.findFirstByKeyword(station.name, MapsService.SearchByKeywordOption())
                    ?: throw IllegalArgumentException("${station.fullname}에 해당하는 장소가 없습니다.")
                println("station found: ${station.fullname} / ${place.name}")
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

private suspend fun fetchAllSearchPlaces(regionInfo: TargetRegionInfo, radiusMeter: Int): SearchPlacesResult {
    return targetCategories
        .fold(
            SearchPlacesResult(
                targetRegionInfo = regionInfo,
                radiusMeters = radiusMeter,
                places = emptyMap(),
                totalNumber = emptyMap(),
            )
        ) { acc, category ->
            println("fetching $category places in ${regionInfo.name}(${regionInfo.lat}, ${regionInfo.lng}) ${radiusMeter}m... ")
            val places = kakaoMapsService.findAllByCategory(
                category,
                option = MapsService.SearchByCategoryOption(
                    MapsService.SearchByCategoryOption.CircleRegion(
                        centerLocation = Location(regionInfo.lng, regionInfo.lat),
                        radiusMeters = radiusMeter,
                        sort = MapsService.SearchByCategoryOption.CircleRegion.Sort.DISTANCE
                    )
                ))
            println("fetching $category places completed. ${places.count()} places found.")
            return@fold acc.copy(
                places = acc.places + (category to places),
                totalNumber = acc.totalNumber + (category to places.count()),
            )
        }
}
