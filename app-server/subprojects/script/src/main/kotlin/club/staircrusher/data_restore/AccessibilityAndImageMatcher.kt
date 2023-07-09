package club.staircrusher.data_restore

import club.staircrusher.accessibility.domain.model.StairInfo
import java.time.Instant
import java.util.UUID

/**
 * 2022년 스프레드시트로 뽑은 데이터와 사진을 createdAt을 기반으로 매칭하기 위한 스크립트.
 *
 * input:
 * - 장소 정보(1층 여부만 누락됨)
 * - 건물 정보
 * - 사진 url w/ createdAt, 장소/건물입구/건물엘베 분류 정보
 *
 * output:
 * - 장소 정보 + 사진 url 목록(two-commas separated)
 * - 건물 정보 + 사진 url 목록(two-commas separated)
 * - 매칭 불가능한 사진 url
 *
 * 로직
 * 1. createdAt 기반으로 -1 ~ +1초 이내의 장소/건물 정보와 사진 정보를 매칭시켜본다.
 * 2. heuristic rule을 기반으로 사진 url을 unique한 장소/건물 정보에 매칭시킨다.
 *   - 사진 기준, 매칭되는 장소/건물이 없으면 그 사진 url은 버린다.
 *   - 사진 기준, 매칭되는 장소/건물이 딱 1개이면 그 장소/건물과 매칭시킨다. 건물과 매칭될 때는 장소/건물입구/건물엘베 분류 정보를 바탕으로 건물입구 사진인지 건물입구 사진인지 판단한다.
 *   - 사진 기준, 매칭되는 장소와 건물이 각각 1개이면, 같은 (장소, 건물)에 매칭되는 모든 사진을 찾아 장소/건물입구/건물엘베 분류 정보를 바탕으로 매칭시킨다.
 *   - 그 이외의 사진은... 분류 불가 ㅜㅜ
 * 3. output에 맞게 가공해서 출력한다.
 *   - 장소 정보 + 사진 url 목록(two-commas separated) -> INSERT 문으로 변경해서 .sql 파일로 떨구기
 *   - 건물 정보 + 사진 url 목록(two-commas separated) -> INSERT 문으로 변경해서 .sql 파일로 떨구기
 *   - 매칭 불가능한 사진 url -> println으로 그냥 출력?
 */
fun main() {
    val imageInfos = readImageInfos()
    val placeAccessibilities = readPlaceAccessibilities()
    val buildingAccessibilities = readBuildingAccessibilities()
    println("image count: ${imageInfos.size}")
    println("place accessibility count: ${placeAccessibilities.size}")
    println("building accessibility count: ${buildingAccessibilities.size}")

    val placeAccessibilityByCreatedAtEpochSeconds = placeAccessibilities.groupBy { it.createdAt.epochSecond }
    val buildingAccessibilityByCreatedAtEpochSeconds = buildingAccessibilities.groupBy { it.createdAt.epochSecond }

    val candidatesByImageInfo = imageInfos.map {
        val paCandidates = placeAccessibilityByCreatedAtEpochSeconds.findCandidates(it)
        val baCandidates = buildingAccessibilityByCreatedAtEpochSeconds.findCandidates(it)
        it to (paCandidates + baCandidates)
    }.toMap()

    val imageAccessibilityPairs = mutableListOf<ImageAccessibilityPair<Accessibility>>()

    // 1. candidate가 없는 애들.
    val noCandidateByImageInfos = candidatesByImageInfo.filter { it.value.isEmpty() }
    // 2. candidate가 1개인 애들은 바로 꽂으면 됨.
    val singleCandidateByImageInfos = candidatesByImageInfo.filter { it.value.size == 1 }
    imageAccessibilityPairs += singleCandidateByImageInfos.mapNotNull { (imageInfo, candidates) ->
        val candidate = candidates[0]
        val matchType = when (imageInfo.matchType) {
            ImageMatchType.PLACE_ACCESSIBILITY -> ImageMatchType.PLACE_ACCESSIBILITY
            ImageMatchType.BUILDING_ACCESSIBILITY_ENTRANCE -> ImageMatchType.BUILDING_ACCESSIBILITY_ENTRANCE
            ImageMatchType.BUILDING_ACCESSIBILITY_ELEVATOR -> ImageMatchType.BUILDING_ACCESSIBILITY_ELEVATOR
            null -> return@mapNotNull null
        }
        ImageAccessibilityPair(
            imageInfo = imageInfo,
            accessibility = candidate,
            matchType = matchType,
        )
    }
    // 3. candidate가 2개인 애들 중 pa 1개와 ba 1개인 애들이 많은데, 데이터를 보면 사진 여러 장의 candidate가 동일함. e.g. 사진 1,2,3,4의 candidate가 pa1, ba1로 동일.
    //    이런 경우에는 사람이 비교적 쉽게 노가다로 해결 가능? or 장소 정보 사진으로 모두 꽂아넣기?
    val onePaOneBaCandidatesByImageInfos = candidatesByImageInfo
        .filter { it.value.size == 2 && it.value.count { it is BuildingAccessibility } == 1 }
        .run {
            this.map { it.value to it.key }
                .groupBy { it.first }
                .mapValues { it.value.map { it.second } }
                .map { it.value to it.key }
                .toMap()
        }
    imageAccessibilityPairs += onePaOneBaCandidatesByImageInfos.flatMap { (imageInfos, candidates) ->
        imageInfos.mapNotNull { imageInfo ->
            val (accessibility, matchType) = when (imageInfo.matchType) {
                ImageMatchType.PLACE_ACCESSIBILITY -> candidates.find { it is PlaceAccessibility }!! to ImageMatchType.PLACE_ACCESSIBILITY
                ImageMatchType.BUILDING_ACCESSIBILITY_ENTRANCE -> candidates.find { it is BuildingAccessibility }!! to ImageMatchType.BUILDING_ACCESSIBILITY_ENTRANCE
                ImageMatchType.BUILDING_ACCESSIBILITY_ELEVATOR -> candidates.find { it is BuildingAccessibility }!! to ImageMatchType.BUILDING_ACCESSIBILITY_ELEVATOR
                null -> return@mapNotNull null
            }
            ImageAccessibilityPair(
                imageInfo = imageInfo,
                accessibility = accessibility,
                matchType = matchType,
            )
        }
    }

    // 4. else
    val alreadyContainedImageInfos = noCandidateByImageInfos.map { it.key }.toSet() +
        singleCandidateByImageInfos.map { it.key }.toSet() +
        onePaOneBaCandidatesByImageInfos.flatMap { it.key }.toSet()
    val remainingCandidatesByImageInfos = candidatesByImageInfo
        .filter { it.key !in alreadyContainedImageInfos }
        .filter { it.value.isNotEmpty() }

    check(imageInfos.size == noCandidateByImageInfos.size + singleCandidateByImageInfos.size + onePaOneBaCandidatesByImageInfos.flatMap { it.key }.size + remainingCandidatesByImageInfos.size)

    println(imageAccessibilityPairs)

    val paMatchResults = imageAccessibilityPairs
        .filter { it.accessibility is PlaceAccessibility }
        .groupBy { it.accessibility }
        .map { (pa, imageAccessibilityPairs) ->
            MatchResult(
                imageInfos = imageAccessibilityPairs.map { it.imageInfo },
                accessibility = pa as PlaceAccessibility,
                matchType = imageAccessibilityPairs[0].matchType,
            )
        }

    val baMatchResults = imageAccessibilityPairs
        .filter { it.accessibility is BuildingAccessibility }
        .groupBy { it.accessibility }
        .map { (ba, imageAccessibilityPairs) ->
            MatchResult(
                imageInfos = imageAccessibilityPairs.map { it.imageInfo },
                accessibility = ba as BuildingAccessibility,
                matchType = imageAccessibilityPairs[0].matchType,
            )
        }

    val paInsertQueries = paMatchResults.joinToString("\n") { it.toInsertQuery() }
    writeTextAsFile(paInsertQueries, "insert_place_accessibilities_2022.sql")

    val baInsertQueries = baMatchResults.joinToString("\n") { it.toInsertQuery() }
    writeTextAsFile(baInsertQueries, "insert_building_accessibilities_2022.sql")



//    noCandidateByImageInfos.forEach {
//        println(it.key.url)
//    }
//    singleCandidateByImageInfos.forEach {
//        println(it.key.url)
//    }
//    onePaOneBaCandidatesByImageInfos.forEach {
//        it.key.forEach {
//            println("${it.createdAt.toKstString()},${it.url}")
//        }
//    }
//
//    println(candidatesByImageInfo.size)
//    // {2=2675, 1=1399, 0=634, 3=152, 4=114}
//    // {2=2674, 1=1299, 4=251, 0=385, 3=359, 6=3, 5=3}
}

private fun readImageInfos(): List<ImageInfo> {
    val imageInfoLines = readTsvAsLines("data_restore/AccessibilityAndImageMatcher/2022_images.tsv")
    return imageInfoLines.map { (createdAtStr, url, matchType) ->
        ImageInfo(
            createdAt = createdAtStr.toInstant(),
            url = url,
            matchType = when (matchType) {
                "0" -> null
                "1" -> ImageMatchType.PLACE_ACCESSIBILITY
                "2" -> ImageMatchType.BUILDING_ACCESSIBILITY_ENTRANCE
                "3" -> ImageMatchType.BUILDING_ACCESSIBILITY_ELEVATOR
                "" -> null
                else -> throw IllegalArgumentException("Invalid match type $matchType for url $url")
            }
        )
    }
}

private fun readPlaceAccessibilities(): List<PlaceAccessibility> {
    // 점포 id,점포 이름,건물 id,주소,출입구 계단 개수,출입구 경사로 유무,생성 시점,입력자 이름
    val lines = readTsvAsLines("data_restore/AccessibilityAndImageMatcher/2022_place_accessibilities.tsv")
    return lines.map { (placeId, _, _, addressStr, stairInfoStr, hasSlopeStr, createdAtStr) ->
        PlaceAccessibility(
            placeId = placeId,
            isFirstFloor = false, // TODO
            stairInfo = stairInfoStr.toStairInfo(),
            hasSlope = hasSlopeStr.toBooleanStrict(),
            imageUrls = emptyList(),
            createdAt = createdAtStr.toInstant(),
        )
    }
}

private fun readBuildingAccessibilities(): List<BuildingAccessibility> {
    // 건물 id,주소,출입구 계단 개수,출입구 경사로 유무,엘리베이터 유무,엘리베이터까지의 계단 개수,생성 시점,입력자 이름
    val lines = readTsvAsLines("data_restore/AccessibilityAndImageMatcher/2022_building_accessibilities.tsv")
    return lines.map { (buildingId, addressStr, entranceStairInfoStr, hasSlopeStr, hasElevatorStr, elevatorStairInfoStr, _, createdAtStr) ->
        BuildingAccessibility(
            buildingId = buildingId,
            entranceStairInfo = entranceStairInfoStr.toStairInfo(),
            entranceImageUrls = emptyList(), // TODO
            hasSlope = hasSlopeStr.toBooleanStrict(),
            hasElevator = hasElevatorStr.toBooleanStrict(),
            elevatorStairInfo = elevatorStairInfoStr.toStairInfo(),
            elevatorImageUrls = emptyList(), // TODO
            createdAt = createdAtStr.toInstant(),
        )
    }
}

private fun <T> Map<Long, List<T>>.findCandidates(imageInfo: ImageInfo): List<T> {
    val imageInfoCreatedAtEpochSeconds = imageInfo.createdAt.epochSecond
    return (-1..1).flatMap {
        this[imageInfoCreatedAtEpochSeconds + it] ?: emptyList()
    }
}

private operator fun List<String>.component6() = get(5)
private operator fun List<String>.component7() = get(6)
private operator fun List<String>.component8() = get(6)

data class ImageAccessibilityPair<T : Accessibility>(
    val imageInfo: ImageInfo,
    val accessibility: T,
    val matchType: ImageMatchType,
)

enum class ImageMatchType {
    PLACE_ACCESSIBILITY,
    BUILDING_ACCESSIBILITY_ENTRANCE,
    BUILDING_ACCESSIBILITY_ELEVATOR,
    ;
}

data class MatchResult<T : Accessibility>(
    val imageInfos: List<ImageInfo>,
    val accessibility: T,
    val matchType: ImageMatchType,
) {
    fun toInsertQuery() = accessibility.toInsertQuery(imageInfos)
}

data class ImageInfo(
    val createdAt: Instant,
    val url: String,
    val matchType: ImageMatchType? = null,
)

interface Accessibility {
    fun toInsertQuery(imageInfos: List<ImageInfo>): String
}

private data class PlaceAccessibility(
    val placeId: String,
    val isFirstFloor: Boolean,
    val stairInfo: StairInfo,
    val hasSlope: Boolean,
    val imageUrls: List<String>,
    val createdAt: Instant,
) : Accessibility {
    override fun toInsertQuery(imageInfos: List<ImageInfo>): String {
        return "INSERT INTO place_accessibility VALUES (" +
            "'${UUID.randomUUID()}'," + // id
            "'$placeId'," + // place_id
            "$isFirstFloor," + // is_first_floor
            "'$stairInfo'," + // stair_info
            "$hasSlope," + // has_slope
            "NULL," + // user_id
            "'${createdAt.toKstString()}'," + // created_at
            "'${createdAt.toKstString()}'," + // updated_at
            "'${imageInfos.joinToString(",,") { it.url }}'," + // image_urls
            "NULL" + // deleted_at
            ");"
    }
}

private data class BuildingAccessibility(
    val buildingId: String,
    val entranceStairInfo: StairInfo,
    val entranceImageUrls: List<String>,
    val hasSlope: Boolean,
    val hasElevator: Boolean,
    val elevatorStairInfo: StairInfo,
    val elevatorImageUrls: List<String>,
    val createdAt: Instant,
) : Accessibility {
    override fun toInsertQuery(imageInfos: List<ImageInfo>): String {
        val (entranceImageUrls, elevatorImageUrls) = imageInfos.partition { it.matchType == ImageMatchType.BUILDING_ACCESSIBILITY_ENTRANCE }
        check(elevatorImageUrls.all { it.matchType == ImageMatchType.BUILDING_ACCESSIBILITY_ELEVATOR })
        return "INSERT INTO building_accessibility VALUES (" +
            "'${UUID.randomUUID()}'," + // id
            "'$buildingId'," + // building_id
            "'$entranceStairInfo'," + // entrance_stair_info
            "$hasSlope," + // has_slope
            "$hasElevator," + // has_elevator
            "'$elevatorStairInfo'," + // elevator_stair_info
            "NULL," + // user_id
            "'${createdAt.toKstString()}'," + // created_at
            "'${createdAt.toKstString()}'," + // updated_at
            "'${entranceImageUrls.joinToString(",,") { it.url }}'," + // entrance_image_urls
            "'${elevatorImageUrls.joinToString(",,") { it.url }}'," + // elevator_image_urls
            "NULL" + // deleted_at
            ");"
    }
}

private fun String.toStairInfo() = when (this) {
    "-" -> StairInfo.UNDEFINED
    "0개" -> StairInfo.NONE
    "1개" -> StairInfo.ONE
    "2~5개" -> StairInfo.TWO_TO_FIVE
    "6개 이상" -> StairInfo.OVER_SIX
    else -> throw IllegalArgumentException("Invalid stairInfo $this")
}
