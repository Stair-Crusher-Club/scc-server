package club.staircrusher.place.domain.model.place

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class PlaceFavorite(
    @Id
    val id: String,
    val userId: String,
    val placeId: String,
    var deletedAt: Instant? = null,
) : TimeAuditingBaseEntity() {
    fun delete() {
        deletedAt = SccClock.instant()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaceFavorite

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "PlaceFavorite(id='$id', userId='$userId', placeId=$placeId, createdAt=$createdAt, updatedAt=$updatedAt, deletedAt=$deletedAt)"
    }
}
