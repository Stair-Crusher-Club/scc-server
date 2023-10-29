package club.staircrusher.challenge.domain.model

import java.time.Instant

class ChallengeRank(
    val id: String,
    val challengeId: String,
    val userId: String,
    var contributionCount: Int,
    var rank: Long,
    var createdAt: Instant,
    var updatedAt: Instant,
)
