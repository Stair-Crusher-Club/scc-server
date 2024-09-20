package club.staircrusher.stdlib.persistence.jpa

import club.staircrusher.stdlib.clock.SccClock
import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.Instant

@MappedSuperclass
class TimeAuditingBaseEntity {
    @Column(updatable = false, nullable = false)
    lateinit var createdAt: Instant

    @Column(nullable = false)
    lateinit var updatedAt: Instant

    @PrePersist
    fun prePersist() {
        val now = SccClock.instant()
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun preUpdate() {
        val now = SccClock.instant()
        updatedAt = now
    }
}
