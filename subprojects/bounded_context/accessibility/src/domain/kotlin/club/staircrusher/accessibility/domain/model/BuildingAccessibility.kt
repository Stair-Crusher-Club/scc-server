package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class BuildingAccessibility(
    val id: String,
    val buildingId: String,
    val entranceStairInfo: StairInfo,
    val hasSlope: Boolean,
    val hasElevator: Boolean,
    val elevatorStairInfo: StairInfo,
    val userId: String?,
    val createdAt: Instant,
)
