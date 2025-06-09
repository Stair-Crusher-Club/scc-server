package club.staircrusher.challenge.domain.model

import java.time.Instant

data class UpdateChallengeRequest(
    val id: String,
    val name: String,
    val isPublic: Boolean,
    val invitationCode: String?,
    val passcode: String?,
    val startsAt: Instant,
    val endsAt: Instant?,
    val goal: Int,
    val milestones: List<Int>,
    val conditions: List<ChallengeCondition>,
    val description: String,
    val crusherGroup: ChallengeCrusherGroup?,
)
