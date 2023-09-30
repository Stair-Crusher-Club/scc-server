package club.staircrusher.first_floor_error_restore

import club.staircrusher.readCsvAsLines
import club.staircrusher.readTsvAsLines

/**
 * 2022년 12월 전에 등록된 정보에 대해, is_first_floor 값이 잘못 들어가는 이슈가 있었다.
 * 이 스크립트는 이를 복구하기 위함이다.
 */
fun main() {
    val placeFloorInfos = run {
        // https://www.data.go.kr/data/15083033/fileData.do <- 여기서 서울 정보만 뺀 것
        val lines = readCsvAsLines("first_floor_error_restore/seoul_place_info.csv")
            .map { it.map { it.replace("\"", "") } } // csv 파일에서 각 값이 쌍따옴표로 감싸져 있다.
        lines.map {
            PlaceFloorInfo(
                placeName = it[1],
                지번주소 = it[24],
                도로명주소 = it[31],
                floor = it[35].toIntOrNull(),
            )
        }
    }
    val placeFloorInfosBy도로명주소 = placeFloorInfos.groupBy { it.도로명주소 }

    val floorInfoWrongPlaceAccessibilities = run {
        // select
        //     *
        // from
        //     place_accessibility pa
        //     join place p on p.id = pa.place_id
        //     join building b on p.building_id = b.id
        // where
        //     pa.created_at < '2022-12-12 00:00:00'
        // ;
        val lines = readTsvAsLines("first_floor_error_restore/floor_info_wrong_place_accessibilities.tsv")
        lines.map {
            FloorInfoWrongPlaceAccessibility(
                placeAccessibilityId = it[0],
                placeName = it[11],
                currentIsFirstFloor = it[2].toBooleanStrict(),
                도로명주소 = buildString {
                    append(it[24].replace("서울", "서울특별시"))
                    append(" ")
                    append(it[25])
                    append(" ")
                    append(it[28])
                    append(" ")
                    append(it[29])
                    if (it[30] != "\"\""){
                        append("-")
                        append(it[30])
                    }
                },
            )
        }
    }

    floorInfoWrongPlaceAccessibilities
        .forEach { placeAccessibility ->
            val placeFloorInfo = placeFloorInfosBy도로명주소[placeAccessibility.도로명주소]?.find {
                it.placeName == placeAccessibility.placeName
                    || placeAccessibility.placeName in it.placeName
                    || it.placeName in placeAccessibility.placeName
            }
                ?: return@forEach
            placeAccessibility.actualIsFirstFloor = placeFloorInfo.floor?.let { it == 1 }
        }
    val (floorInfoChecked, floorInfoNotChecked) = floorInfoWrongPlaceAccessibilities
        .partition { it.isFloorInfoChecked() }
    val updateQueries = floorInfoChecked
        .filter { it.isFloorInfoWrong() }
        .map {
            "UPDATE place_accessibility SET is_first_floor = ${it.actualIsFirstFloor} WHERE id = '${it.placeAccessibilityId}'";
        }
    println(updateQueries)
    println(floorInfoNotChecked.size)
}

private data class PlaceFloorInfo(
    val placeName: String,
    val 지번주소: String,
    val 도로명주소: String,
    val floor: Int?,
)

private data class FloorInfoWrongPlaceAccessibility(
    val placeAccessibilityId: String,
    val placeName: String,
    val currentIsFirstFloor: Boolean,
    val 도로명주소: String,
) {
    var actualIsFirstFloor: Boolean? = null

    fun isFloorInfoChecked(): Boolean {
        return actualIsFirstFloor != null
    }

    fun isFloorInfoWrong(): Boolean {
        return actualIsFirstFloor != null && currentIsFirstFloor != actualIsFirstFloor
    }
}

// df63592e-e2c8-489f-8859-96fd2c0b503c -> true
