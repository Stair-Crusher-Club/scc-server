package club.staircrusher.accessibility.domain.model

import java.time.Instant

class PlaceAccessibilityUpvote(
    val id: String,
    val userId: String,
    val placeAccessibility: PlaceAccessibility,
    var createdAt: Instant,
    var deletedAt: Instant? = null,
) {
    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is PlaceAccessibilityUpvote && other.id == id
    }

    fun cancel(at: Instant) {
        deletedAt = at
    }
}
