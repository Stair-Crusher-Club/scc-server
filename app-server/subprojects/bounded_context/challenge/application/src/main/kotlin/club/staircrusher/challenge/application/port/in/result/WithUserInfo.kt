package club.staircrusher.challenge.application.port.`in`.result

import club.staircrusher.challenge.application.port.`in`.ChallengeParticipant

data class WithUserInfo<T>(
    val value: T,
    val challengeParticipant: ChallengeParticipant?,
)
