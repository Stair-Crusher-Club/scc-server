package club.staircrusher.place.domain.model.accessibility

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class PlaceAccessibilityUpvote(
    @Id
    val id: String,
    val userId: String,
    val placeAccessibilityId: String,
    var createdAt: Instant,
    var deletedAt: Instant? = null,
) {
    fun cancel(at: Instant) {
        deletedAt = at
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceAccessibilityUpvote

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "PlaceAccessibilityUpvote(id='$id', userId='$userId', placeAccessibilityId='$placeAccessibilityId', " +
            "createdAt=$createdAt, deletedAt=$deletedAt)"
    }
}
