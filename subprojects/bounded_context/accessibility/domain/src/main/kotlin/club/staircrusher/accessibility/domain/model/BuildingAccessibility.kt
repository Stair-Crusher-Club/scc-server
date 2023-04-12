package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class BuildingAccessibility(
    val id: String,
    val buildingId: String,
    val entranceStairInfo: StairInfo,
    val entranceImageUrls: List<String>,
    val hasSlope: Boolean,
    val hasElevator: Boolean,
    val elevatorStairInfo: StairInfo,
    val elevatorImageUrls: List<String>,
    val userId: String?,
    val createdAt: Instant,
    val deletedAt: Instant? = null,
)
