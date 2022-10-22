package club.staircrusher.accessibility.domain.model

import java.time.Instant

class BuildingAccessibilityUpvote(
    val id: String,
    val userId: String,
    val buildingAccessibility: BuildingAccessibility,
    var createdAt: Instant,
    var deletedAt: Instant? = null,
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is BuildingAccessibilityUpvote && other.id == id
    }

    fun cancel(now: Instant) {
        deletedAt = now
    }
}
