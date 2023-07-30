package club.staircrusher.place_count

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.io.File

private const val kakaoApiKey = "fake key"

private data class TargetRegionInfo(
    val name: String,
    val lng: Double,
    val lat: Double,
)

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
    val count: Int,
) {
    fun toCsvRow(): String {
        return "${targetRegionInfo.name}(${radiusMeters}m),$count"
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
private val radiusMetersList = listOf(500, 750, 1000)

fun main() {
    val regionInfos = getTargetRegionInfos()
    val rows = getTargetRegionPlaceCounts(regionInfos).map { it.toCsvRow() }

    val headerRow = "," + targetCategoryGroups.joinToString(",")
    println(listOf(headerRow) + rows)

    File("result.csv").writeText(buildString {
        appendLine(headerRow)
        rows.forEach { appendLine(it) }
    })
}

private fun getTargetRegionInfos(): List<TargetRegionInfo> {
    return listOf(
        TargetRegionInfo(name = "강남역", lng = 127.0276309, lat = 37.4978948),
        TargetRegionInfo(name = "성수역", lng = 127.055974, lat = 37.544569),
        TargetRegionInfo(name = "뚝섬역", lng = 127.047405, lat = 37.547206),
        TargetRegionInfo(name = "서울숲역", lng = 127.044566, lat = 37.5431275),
        TargetRegionInfo(name = "홍대입구역", lng = 126.9244669, lat = 37.557527),
        TargetRegionInfo(name = "연남동", lng = 126.9220027, lat = 37.5644783),
        TargetRegionInfo(name = "종각역", lng = 126.983197, lat = 37.570176),
        TargetRegionInfo(name = "용산역", lng = 126.9648019, lat = 37.5298837),
        TargetRegionInfo(name = "신사역", lng = 127.0200228, lat = 37.5162873),
        TargetRegionInfo(name = "압구정역", lng = 127.028513, lat = 37.52633),
        TargetRegionInfo(name = "압구정로데오역", lng = 127.040572, lat = 37.527394),
    )
}

@Suppress("MagicNumber")
private fun getTargetRegionPlaceCounts(regionInfos: List<TargetRegionInfo>): List<TargetRegionPlaceCount> {
    return regionInfos.flatMap { regionInfo ->
        radiusMetersList.map { radiusMeters ->
            val placeCounts = targetCategoryGroups.joinToString(",") { categoryGroup ->
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

                when (result) {
                    is Result.Failure -> {
                        println(result.getException())
                        "-1"
                    }
                    is Result.Success -> {
                        Regex("\"total_count\":\\s*(\\d*)").find(result.value)?.groups?.get(1)?.value ?: "-1"
                    }
                }
            }
            radiusMeters to placeCounts
        }
            .map { (radiusMeters, placeCounts) -> TargetRegionPlaceCount(regionInfo, radiusMeters, placeCounts.toInt()) }
    }
}
