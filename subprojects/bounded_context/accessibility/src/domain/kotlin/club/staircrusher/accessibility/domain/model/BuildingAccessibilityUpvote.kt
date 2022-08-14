package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class BuildingAccessibilityUpvote(
    val id: String,
    val userId: String,
    val buildingAccessibility: BuildingAccessibility,
    var createdAt: Instant,
    var deletedAt: Instant? = null,
)
