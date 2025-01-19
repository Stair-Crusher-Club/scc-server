package club.staircrusher.challenge.application.port.`in`

import club.staircrusher.user.domain.model.IdentifiedUser

data class ChallengeParticipant(
    val userId: String,
    val nickname: String,
)
fun IdentifiedUser.toDomainModel() = ChallengeParticipant(
    userId = id,
    nickname = nickname,
)
