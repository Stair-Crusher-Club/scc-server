package club.staircrusher.challenge.application.port.`in`

import club.staircrusher.user.domain.model.UserProfile

data class ChallengeParticipant(
    val userId: String,
    val nickname: String,
)
fun UserProfile.toDomainModel() = ChallengeParticipant(
    userId = id,
    nickname = nickname,
)
