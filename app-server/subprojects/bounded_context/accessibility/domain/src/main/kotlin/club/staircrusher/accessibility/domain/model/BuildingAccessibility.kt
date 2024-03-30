package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class BuildingAccessibility(
    val id: String,
    val buildingId: String,
    val entranceStairInfo: StairInfo,
    val entranceStairHeightLevel: StairHeightLevel?,
    val entranceImageUrls: List<String>,
    val hasSlope: Boolean,
    val hasElevator: Boolean,
    val entranceDoorTypes: List<EntranceDoorType>?,
    val elevatorStairInfo: StairInfo,
    val elevatorStairHeightLevel: StairHeightLevel?,
    val elevatorImageUrls: List<String>,
    val userId: String?,
    val createdAt: Instant,
    val deletedAt: Instant? = null,
)
