package club.staircrusher.challenge.domain.model

import java.time.Instant

data class ChallengeQuestContribution(
    val contributionId: String,
    val actionType: ChallengeActionCondition.Type,
    val contributedAt: Instant,
)
