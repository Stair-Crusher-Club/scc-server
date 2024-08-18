package club.staircrusher.accessibility.domain.model

import club.staircrusher.stdlib.clock.SccClock
import jakarta.persistence.Entity
import jakarta.persistence.Id
import java.time.Instant

@Entity
class AccessibilityRank(
    @Id
    val id: String,
    val userId: String,
    conqueredCount: Int,
    rank: Long,
    val createdAt: Instant,
    updatedAt: Instant,
) {
    var conqueredCount: Int = conqueredCount
        protected set

    var rank: Long = rank
        protected set

    var updatedAt: Instant = updatedAt
        protected set

    fun updateConqueredCount(conqueredCount: Int) {
        this.conqueredCount = conqueredCount
        updatedAt = SccClock.instant()
    }

    fun updateRank(rank: Long) {
        this.rank = rank
        updatedAt = SccClock.instant()
    }
}
