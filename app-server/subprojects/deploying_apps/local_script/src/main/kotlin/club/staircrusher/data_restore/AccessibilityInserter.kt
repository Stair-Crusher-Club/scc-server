package club.staircrusher.data_restore

import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.infra.adapter.out.web.KakaoMapsService
import club.staircrusher.place.infra.adapter.out.web.KakaoProperties
import club.staircrusher.readTsvAsLines
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.toKstString
import club.staircrusher.writeTextAsFile
import kotlinx.coroutines.runBlocking
import java.time.Instant

val kakaoMapsService = KakaoMapsService(
    KakaoProperties("dummy")
)

fun main() {
    writePlaceAccessibilityInsertQueries()
    writeBuildingAccessibilityInsertQueries()
}

private fun writePlaceAccessibilityInsertQueries() {
    val places = parseTsvToPlaceList()
    writeTextAsFile(places.joinToString("\n") { it.toInsertQuery() }, "insert_place_accessibilities_2023.sql")
}

private fun parseTsvToPlaceList(): List<PlaceAccessibility> {
    val lines = readTsvAsLines("data_restore/AccessibilityInserter/place_accessibilities.tsv")
    // 행 고유번호	빈 column	빈 column	담당자	생성시간	윗 사진과의 등록 시차	이미지	건물장소번호	식별정보 1	장소명 (카카오지도명과 동일하게 입력	도로명 주소	1층여부: 1층=1, 1층 아님 2	계단 수 (눈에 보이는 숫자)	경사로 유무 = 있음 1, 없음 2
    val errorMessages = mutableListOf<String>()
    val result = lines.map { line ->
        val imageUrls = line[1].split(",")
        val images = imageUrls.map { AccessibilityImage(type = AccessibilityImage.Type.PLACE, imageUrl = it, thumbnailUrl = null) }
        val placeName = line[2]
        val address = line[3]
        val isFirstFloor = when (val rawIsFirstFloor = line[4]) {
            "1" -> true
            "2" -> false
            else -> throw IllegalArgumentException("Invalid isFirstFloor value: $rawIsFirstFloor")
        }
        val stairInfo = line[5].parseToStairInfo()
        val hasSlope = when (val rawHasSlope = line[6]) {
            "1" -> true
            "2" -> false
            else -> throw IllegalArgumentException("Invalid hasSlope value: $rawHasSlope")
        }
        val userName = line[9]
        val userId = userName.parseToUserId()

        // TODO: 새로운 필드 채우기?
        Pair(placeName, PlaceAccessibility(
            id = EntityIdGenerator.generateRandom(),
            placeId = "", // 지도 api를 대량으로 호출하기 전에 파싱 성공을 확인하기 위해 placeId를 제외한 필드부터 채운다.
            floors = null,
            isFirstFloor = isFirstFloor,
            isStairOnlyOption = null,
            stairInfo = stairInfo,
            stairHeightLevel = null,
            hasSlope = hasSlope,
            imageUrls = imageUrls,
            images = images,
            userId = userId,
            createdAt = Instant.now(),
            entranceDoorTypes = null,
            deletedAt = null,
        ))
    }
        .map { (placeName, placeAccessibility) ->
            val placeId: String by lazy {
                runBlocking {
                    kakaoMapsService.findFirstByKeyword(placeName, option = MapsService.SearchByKeywordOption())
                }?.id ?: run {
                    errorMessages.add("No matching place for $placeName")
                    ""
                }
                // TODO: address랑 검색 결과의 주소랑 비교하기?
            }
            placeAccessibility.copy(placeId = placeId)
        }
        .filter { it.placeId.isNotBlank() }
        .groupBy { it.placeId }
        .map { (_, value) ->
            value[0].copy(imageUrls = value.flatMap { it.imageUrls }.distinct())
        }

    System.err.println(errorMessages)

    return result
}

private fun PlaceAccessibility.toInsertQuery(): String {
    return "INSERT INTO place_accessibility VALUES (" +
        "'$id'," + // id
        "'$placeId'," + // place_id
        "$isFirstFloor," + // is_first_floor
        "'$stairInfo'," + // stair_info
        "$hasSlope," + // has_slope
        "NULL," + // user_id
        "'${createdAt.toKstString()}'," + // created_at
        "'${createdAt.toKstString()}'," + // updated_at
        "'${imageUrls.joinToString(",,")}'," + // image_urls
        "NULL" + // deleted_at
        ");"
}

private fun writeBuildingAccessibilityInsertQueries() {
    val buildingAccessibilities = parseTsvToBuildingList()
    writeTextAsFile(buildingAccessibilities.joinToString("\n") { it.toInsertQuery() }, "insert_building_accessibilities_2023.sql")
}

private fun parseTsvToBuildingList(): List<BuildingAccessibility> {
    val lines = readTsvAsLines("data_restore/AccessibilityInserter/building_accessibilities.tsv")
    // 행 고유번호	빈 column	빈 column	담당자	생성시간	윗 사진과의 등록 시차	이미지	건물장소번호	식별정보 2	건물 추측한 방법 (ㅇㅇ빌딩, 장소기반 등)	건물 도로명 주소	입구계단수	경사로 유무 = 있음 1, 없음 2	엘리베이터 유무 = 있음 1, 없음 2	엘리베이터까지 계단 개수
    return lines.map { line ->
        val imageUrls = line[6].split(",")
        val roadAddress = line[10]
        val entranceStairInfo = line[11].parseToStairInfo()
        val hasSlope = when (val rawHasSlope = line[12]) {
            "1" -> true
            "2" -> false
            else -> throw IllegalArgumentException("Invalid isFirstFloor value: $rawHasSlope")
        }
        val hasElevator = when (val rawHasElevator = line[13]) {
            "1" -> true
            "2" -> false
            else -> throw IllegalArgumentException("Invalid hasElevator value: $rawHasElevator")
        }
        val elevatorStairInfo = line[14].parseToStairInfo()

        // TODO: 새로운 필드 채우기?
        BuildingAccessibility(
            id = EntityIdGenerator.generateRandom(),
            buildingId = Building.generateId(roadAddress),
            entranceStairInfo = entranceStairInfo,
            entranceStairHeightLevel = null,
            entranceImageUrls = imageUrls, // TODO: 입구 사진과 엘레베이터 사진으로 나누기
            hasSlope = hasSlope,
            hasElevator = hasElevator,
            entranceDoorTypes = emptyList(),
            elevatorStairInfo = elevatorStairInfo,
            elevatorStairHeightLevel = null,
            elevatorImageUrls = emptyList(),
            images = emptyList(),
            userId = null,
            createdAt = Instant.now(),
            deletedAt = null,
        )
    }
        .groupBy { it.buildingId }
        .map { (_, value) ->
            value[0].copy(
                entranceImageUrls = value.flatMap { it.entranceImageUrls }.distinct(),
                elevatorImageUrls = value.flatMap { it.elevatorImageUrls }.distinct(),
            )
        }
}

private fun BuildingAccessibility.toInsertQuery(): String {
    return "INSERT INTO building_accessibility VALUES (" +
        "'$id'," + // id
        "'$buildingId'," + // building_id
        "'$entranceStairInfo'," + // entrance_stair_info
        "$hasSlope," + // has_slope
        "$hasElevator," + // has_elevator
        "'$elevatorStairInfo'," + // elevator_stair_info
        "NULL," + // user_id
        "'${createdAt.toKstString()}'," + // created_at
        "'${createdAt.toKstString()}'," + // updated_at
        "'${entranceImageUrls.joinToString(",,")}'," + // entrance_image_urls
        "'${elevatorImageUrls.joinToString(",,")}'," + // elevator_image_urls
        "NULL" + // deleted_at
        ");"
}

private fun String.parseToStairInfo() = when (this.toInt()) {
    0 -> StairInfo.NONE
    1 -> StairInfo.ONE
    in 2..5 -> StairInfo.TWO_TO_FIVE
    in 6..Int.MAX_VALUE -> StairInfo.OVER_SIX
    else -> throw IllegalArgumentException("Invalid stairInfo value: $this")
}

private fun String.parseToUserId() = when (this) {
    "" -> null
    "23봄시즌" -> "6d0385e8-551a-45d4-bfb3-0829e22637ca"
    "23봄시즌_강남지부" -> "e81b400b-5814-4e65-823b-8420be623f9e"
    "23봄시즌_관악지부" -> "5078c2e3-0ecd-41bb-a114-fbc608b9d04a"
    "23봄시즌_성남지부" -> "45345588-2e31-4e69-9e31-23a229b806ec"
    "23봄시즌_도봉지부" -> "00d8357c-c936-4311-9517-962c9415387a"
    else -> throw IllegalArgumentException("Invalid userName value: $this")
}
