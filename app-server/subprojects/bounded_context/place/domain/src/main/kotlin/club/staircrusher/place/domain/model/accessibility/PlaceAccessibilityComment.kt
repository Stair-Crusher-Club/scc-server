package club.staircrusher.place.domain.model.accessibility

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class PlaceAccessibilityComment(
    @Id
    val id: String,
    val placeId: String,
    val userId: String?,
    val comment: String,
    val createdAt: Instant,
    val deletedAt: Instant? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceAccessibilityComment

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "PlaceAccessibilityComment(id='$id', placeId='$placeId', userId=$userId, comment='$comment', " +
            "createdAt=$createdAt, deletedAt=$deletedAt)"
    }
}
