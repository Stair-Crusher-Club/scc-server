package club.staircrusher.challenge.domain.model

import java.time.Instant

data class UpdateChallengeRequest(
    val id: String,
    val name: String,
    val endsAt: Instant?,
    val description: String,
    val crusherGroup: ChallengeCrusherGroup?,
    val quests: List<ChallengeQuest>?,
)
