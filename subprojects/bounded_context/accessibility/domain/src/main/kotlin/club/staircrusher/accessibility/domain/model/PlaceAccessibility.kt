package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class PlaceAccessibility(
    val id: String,
    val placeId: String,
    val isFirstFloor: Boolean,
    val stairInfo: StairInfo,
    val hasSlope: Boolean,
    val imageUrls: List<String>,
    val userId: String?,
    val createdAt: Instant,
)
