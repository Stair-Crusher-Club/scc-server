package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class AccessibilityRank(
    val id: String,
    val userId: String,
    val conqueredCount: Int,
    val rank: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
)
