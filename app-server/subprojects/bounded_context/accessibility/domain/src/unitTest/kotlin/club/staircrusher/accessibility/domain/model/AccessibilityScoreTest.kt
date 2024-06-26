package club.staircrusher.accessibility.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.Instant

class AccessibilityScoreTest {


    @Test
    fun test() {
        assertEquals(
            1.0, AccessibilityScore.get(
                mockPa.copy(
                    isFirstFloor = true,
                    hasSlope = false,
                    stairInfo = StairInfo.ONE,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                mockBa,
            )
        )
        assertEquals(
            1.0, AccessibilityScore.get(
                mockPa.copy(
                    isFirstFloor = true,
                    hasSlope = true,
                    stairInfo = StairInfo.ONE,
                    stairHeightLevel = StairHeightLevel.THUMB
                ),
                mockBa,
            )
        )
        assertEquals(
            4.0, AccessibilityScore.get(
                mockPa.copy(
                    isFirstFloor = true,
                    hasSlope = false,
                    stairInfo = StairInfo.TWO_TO_FIVE,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                mockBa,
            )
        )
        assertEquals(
            3.0, AccessibilityScore.get(
                mockPa.copy(
                    isFirstFloor = true,
                    hasSlope = true,
                    stairInfo = StairInfo.OVER_SIX,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                mockBa,
            )
        )
        assertEquals(
            0.5 + 1.0 + 5.0, AccessibilityScore.get(
                mockPa.copy(
                    isFirstFloor = false,
                    hasSlope = true,
                    stairInfo = StairInfo.ONE,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                mockBa.copy(
                    hasElevator = false,
                    entranceStairInfo = StairInfo.ONE,
                    entranceStairHeightLevel = StairHeightLevel.THUMB,
                    hasSlope = true
                ),
            )
        )
        assertEquals(
            0.5 + 1.0 + 1.0, AccessibilityScore.get(
                mockPa.copy(
                    isFirstFloor = false,
                    hasSlope = true,
                    stairInfo = StairInfo.ONE,
                    stairHeightLevel = StairHeightLevel.HALF_THUMB
                ),
                mockBa.copy(
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

    private val mockPa = PlaceAccessibility(
        id = "",
        placeId = "",
        floors = emptyList(),
        isFirstFloor = false,
        isStairOnlyOption = false,
        stairInfo = StairInfo.ONE,
        stairHeightLevel = StairHeightLevel.THUMB,
        hasSlope = false,
        entranceDoorTypes = emptyList(),
        imageUrls = emptyList(),
        images = emptyList(),
        userId = null,
        createdAt = Instant.now(),
        deletedAt = null
    )

    private val mockBa = BuildingAccessibility(
        id = "",
        buildingId = "",
        entranceStairInfo = StairInfo.ONE,
        entranceStairHeightLevel = null,
        entranceImageUrls = emptyList(),
        hasSlope = false,
        hasElevator = false,
        entranceDoorTypes = emptyList(),
        elevatorImageUrls = emptyList(),
        images = emptyList(),
        elevatorStairInfo = StairInfo.ONE,
        elevatorStairHeightLevel = StairHeightLevel.THUMB,
        userId = null,
        createdAt = Instant.now(),
        deletedAt = null
    )
}
