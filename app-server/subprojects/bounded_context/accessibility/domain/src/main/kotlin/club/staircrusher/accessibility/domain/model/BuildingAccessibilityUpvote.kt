package club.staircrusher.accessibility.domain.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class BuildingAccessibilityUpvote(
    @Id
    val id: String,
    val userId: String,
    val buildingAccessibilityId: String,
    var createdAt: Instant,
    var deletedAt: Instant? = null,
) {
    fun cancel(now: Instant) {
        deletedAt = now
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BuildingAccessibilityUpvote

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "BuildingAccessibilityUpvote(id='$id', userId='$userId', " +
            "buildingAccessibilityId='$buildingAccessibilityId', createdAt=$createdAt, deletedAt=$deletedAt)"
    }
}
