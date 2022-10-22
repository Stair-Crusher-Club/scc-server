package club.staircrusher.user.domain.service

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.domain.model.User
import club.staircrusher.user.domain.model.UserAccessTokenPayload
import club.staircrusher.user.domain.exception.UserAuthenticationException
import club.staircrusher.user.domain.exception.TokenVerificationException

@Component
class UserAuthService(
    private val tokenManager: TokenManager,
) {
    fun issueAccessToken(user: User): String {
        return tokenManager.issueToken(UserAccessTokenPayload(userId = user.id))
    }

    @Throws(UserAuthenticationException::class)
    fun verifyAccessToken(token: String): UserAccessTokenPayload {
        return try {
            tokenManager.verify(token, UserAccessTokenPayload::class)
        } catch (_: TokenVerificationException) {
            throw UserAuthenticationException()
        }
    }
}
