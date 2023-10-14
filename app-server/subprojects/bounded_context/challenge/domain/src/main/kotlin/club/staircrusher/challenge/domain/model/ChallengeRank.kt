package club.staircrusher.challenge.domain.model

import java.time.Instant

data class ChallengeRank(
    val id: String,
    val challengeId: String,
    val userId: String,
    val contributionCount: Int,
    val rank: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
)
