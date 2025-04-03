package club.staircrusher.place.domain.model.accessibility

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class BuildingAccessibilityComment(
    @Id
    val id: String,
    val buildingId: String,
    val userId: String?,
    val comment: String,
    val createdAt: Instant,
    val deletedAt: Instant? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BuildingAccessibilityComment

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "BuildingAccessibilityComment(id='$id', buildingId='$buildingId', userId=$userId, comment='$comment', " +
            "createdAt=$createdAt, deletedAt=$deletedAt)"
    }
}
