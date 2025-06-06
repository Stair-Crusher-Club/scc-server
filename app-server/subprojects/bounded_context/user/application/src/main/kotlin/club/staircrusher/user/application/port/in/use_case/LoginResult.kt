package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.user.domain.model.AuthTokens
import club.staircrusher.user.domain.model.UserProfile

data class LoginResult(
    val authTokens: AuthTokens,
    val userProfile: UserProfile,
)
