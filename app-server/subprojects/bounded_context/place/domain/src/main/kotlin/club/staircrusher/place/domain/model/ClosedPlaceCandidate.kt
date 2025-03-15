package club.staircrusher.place.domain.model

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.persistence.jpa.TimeAuditingBaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class ClosedPlaceCandidate(
    @Id
    override val id: String,

    @Column(nullable = false)
    val placeId: String,

    @Column(nullable = false)
    val externalId: String,

    @Column(nullable = false)
    val originalName: String,

    @Column(nullable = false)
    val originalAddress: String,

    @Column(nullable = false)
    val closedAt: Instant,

    @Column(nullable = true)
    var acceptedAt: Instant? = null,

    @Column(nullable = true)
    var ignoredAt: Instant? = null,
) : TimeAuditingBaseEntity() {
    fun accept() {
        acceptedAt = SccClock.instant()
    }

    fun ignore() {
        ignoredAt = SccClock.instant()
    }
}
