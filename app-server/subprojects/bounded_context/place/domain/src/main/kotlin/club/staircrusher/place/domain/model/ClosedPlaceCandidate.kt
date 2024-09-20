package club.staircrusher.place.domain.model

import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class ClosedPlaceCandidate(
    @Id
    val id: String,

    @Column(nullable = false)
    val placeId: String,
) : TimeAuditingBaseEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClosedPlaceCandidate

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "ClosedPlaceCandidate(id='$id', placeId='$placeId', createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}
