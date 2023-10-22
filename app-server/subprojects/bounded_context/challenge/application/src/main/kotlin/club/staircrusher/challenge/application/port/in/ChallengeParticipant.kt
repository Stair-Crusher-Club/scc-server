package club.staircrusher.challenge.application.port.`in`

import club.staircrusher.user.domain.model.User

data class ChallengeParticipant(
    val userId: String,
    val nickname: String,
)
fun User.toDomainModel() = ChallengeParticipant(
    userId = id,
    nickname = nickname,
)
