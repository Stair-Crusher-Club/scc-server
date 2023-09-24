package club.staircrusher.challenge.domain.model

data class CreateChallengeRequest(
    val name: String,
    val isPublic: Boolean,
    val invitationCode: String?,
    val passcode: String?,
    val startsAtMillis: Long,
    val endsAtMillis: Long?,
    val goal: Int,
    val milestones: List<Int>,
)
