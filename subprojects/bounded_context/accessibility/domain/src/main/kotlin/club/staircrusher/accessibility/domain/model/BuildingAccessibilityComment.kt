package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class BuildingAccessibilityComment(
    val id: String,
    val buildingId: String,
    val userId: String?,
    val comment: String,
    val createdAt: Instant,
    val deletedAt: Instant? = null,
)
