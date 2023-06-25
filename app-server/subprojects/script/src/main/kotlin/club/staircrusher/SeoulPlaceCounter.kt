package club.staircrusher

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.io.File

data class QuestTargetRegionInfo(
    val name: String,
    val lng: Double,
    val lat: Double,
)

data class CategoryGroup(
    val name: String,
    val code: String,
) {
    override fun toString(): String {
        return "$name($code)"
    }
}

@Suppress("LongMethod")
fun main() {
    val regionInfos = listOf(
        QuestTargetRegionInfo(name = "강남역", lng = 127.0276309, lat = 37.4978948),
        QuestTargetRegionInfo(name = "성수역", lng = 127.055974, lat = 37.544569),
        QuestTargetRegionInfo(name = "뚝섬역", lng = 127.047405, lat = 37.547206),
        QuestTargetRegionInfo(name = "서울숲역", lng = 127.044566, lat = 37.5431275),
        QuestTargetRegionInfo(name = "홍대입구역", lng = 126.9244669, lat = 37.557527),
        QuestTargetRegionInfo(name = "연남동", lng = 126.9220027, lat = 37.5644783),
        QuestTargetRegionInfo(name = "종각역", lng = 126.983197, lat = 37.570176),
        QuestTargetRegionInfo(name = "용산역", lng = 126.9648019, lat = 37.5298837),
        QuestTargetRegionInfo(name = "신사역", lng = 127.0200228, lat = 37.5162873),
        QuestTargetRegionInfo(name = "압구정역", lng = 127.028513, lat = 37.52633),
        QuestTargetRegionInfo(name = "압구정로데오역", lng = 127.040572, lat = 37.527394),
    )
    @Suppress("MagicNumber")
    val radiusMetersList = listOf(500, 750, 1000)
    val targetCategoryGroups = listOf(
        CategoryGroup(name = "음식점", code = "FD6"),
        CategoryGroup(name = "카페", code = "CE7"),
        CategoryGroup(name = "약국", code = "PM9"),
        CategoryGroup(name = "병원", code = "HP8"),
        CategoryGroup(name = "편의점", code = "CS2"),
    )

    @Suppress("MagicNumber")
    val rows = regionInfos.flatMap { regionInfo ->
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
                        "Authorization" to "KakaoAK <fake key>",
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
            .map { (radiusMeters, placeCounts) -> "${regionInfo.name}(${radiusMeters}m),$placeCounts" }
    }

    val headerRow = "," + targetCategoryGroups.joinToString(",")
    println(listOf(headerRow) + rows)

    File("result.csv").writeText(buildString {
        appendLine(headerRow)
        rows.forEach { appendLine(it) }
    })
}
