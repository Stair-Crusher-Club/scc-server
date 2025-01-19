package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.user.domain.model.AuthTokens
import club.staircrusher.user.domain.model.IdentifiedUser

data class LoginResult(
    val authTokens: AuthTokens,
    val user: IdentifiedUser,
)
