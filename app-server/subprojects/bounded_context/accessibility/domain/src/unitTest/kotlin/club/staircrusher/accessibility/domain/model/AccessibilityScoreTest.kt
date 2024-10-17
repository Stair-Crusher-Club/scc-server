package club.staircrusher.accessibility.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class AccessibilityScoreTest {


    @Test
    fun test() {
        assertEquals(
            2.0, AccessibilityScore.get(
                createPa(
                    isFirstFloor = true,
                    hasSlope = false,
                    stairInfo = StairInfo.ONE,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                createBa(),
            )
        )
        assertEquals(
            1.0, AccessibilityScore.get(
                createPa(
                    isFirstFloor = true,
                    hasSlope = true,
                    stairInfo = StairInfo.ONE,
                    stairHeightLevel = StairHeightLevel.THUMB
                ),
                createBa(),
            )
        )
        assertEquals(
            3.0, AccessibilityScore.get(
                createPa(
                    isFirstFloor = true,
                    hasSlope = false,
                    stairInfo = StairInfo.TWO_TO_FIVE,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                createBa(),
            )
        )
        assertEquals(
            1.0, AccessibilityScore.get(
                createPa(
                    isFirstFloor = true,
                    hasSlope = true,
                    stairInfo = StairInfo.OVER_SIX,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                createBa(),
            )
        )
        assertEquals(
            5.0, AccessibilityScore.get(
                createPa(
                    isFirstFloor = false,
                    hasSlope = true,
                    stairInfo = StairInfo.ONE,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                createBa(
                    hasElevator = false,
                    entranceStairInfo = StairInfo.ONE,
                    entranceStairHeightLevel = StairHeightLevel.THUMB,
                    hasSlope = true
                ),
            )
        )
        assertEquals(
            2.0, AccessibilityScore.get(
                createPa(
                    isFirstFloor = false,
                    hasSlope = true,
                    stairInfo = StairInfo.ONE,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                createBa(
                    hasElevator = true,
                    entranceStairInfo = StairInfo.ONE,
                    entranceStairHeightLevel = StairHeightLevel.THUMB,
                    elevatorStairInfo = StairInfo.ONE,
                    elevatorStairHeightLevel = StairHeightLevel.THUMB,
                    hasSlope = true
                ),
            )
        )
    }

    private fun createPa(
        id: String = "",
        placeId: String = "",
        floors: List<Int>? = emptyList(),
        isFirstFloor: Boolean = false,
        isStairOnlyOption: Boolean? = false,
        stairInfo: StairInfo = StairInfo.ONE,
        stairHeightLevel: StairHeightLevel? = StairHeightLevel.THUMB,
        hasSlope: Boolean = false,
        entranceDoorTypes: List<EntranceDoorType>? = emptyList(),
        imageUrls: List<String> = emptyList(),
        images: List<AccessibilityImage> = emptyList(),
        userId: String? = null,
        createdAt: Instant = Instant.now(),
        deletedAt: Instant? = null
    ) = PlaceAccessibility(
        id = id,
        placeId = placeId,
        floors = floors,
        isFirstFloor = isFirstFloor,
        isStairOnlyOption = isStairOnlyOption,
        stairInfo = stairInfo,
        stairHeightLevel = stairHeightLevel,
        hasSlope = hasSlope,
        entranceDoorTypes = entranceDoorTypes,
        imageUrls = imageUrls,
        images = images,
        userId = userId,
        createdAt = createdAt,
        deletedAt = deletedAt,
    )

    private fun createBa(
        id: String = "",
        buildingId: String = "",
        entranceStairInfo: StairInfo = StairInfo.ONE,
        entranceStairHeightLevel: StairHeightLevel? = null,
        entranceImageUrls: List<String> = emptyList(),
        entranceImages: List<AccessibilityImage> = emptyList(),
        hasSlope: Boolean = false,
        hasElevator: Boolean = false,
        entranceDoorTypes: List<EntranceDoorType>? = emptyList(),
        elevatorStairInfo: StairInfo = StairInfo.ONE,
        elevatorStairHeightLevel: StairHeightLevel? = StairHeightLevel.THUMB,
        elevatorImageUrls: List<String> = emptyList(),
        elevatorImages: List<AccessibilityImage> = emptyList(),
        userId: String? = null,
        createdAt: Instant = Instant.now(),
        deletedAt: Instant? = null,
    ) = BuildingAccessibility(
        id = id,
        buildingId = buildingId,
        entranceStairInfo = entranceStairInfo,
        entranceStairHeightLevel = entranceStairHeightLevel,
        entranceImageUrls = entranceImageUrls,
        entranceImages = entranceImages,
        hasSlope = hasSlope,
        hasElevator = hasElevator,
        entranceDoorTypes = entranceDoorTypes,
        elevatorStairInfo = elevatorStairInfo,
        elevatorStairHeightLevel = elevatorStairHeightLevel,
        elevatorImageUrls = elevatorImageUrls,
        elevatorImages = elevatorImages,
        userId = userId,
        createdAt = createdAt,
        deletedAt = deletedAt,
    )
}
