package club.staircrusher.user.domain.service

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.token.TokenManager
import club.staircrusher.user.domain.model.User
import club.staircrusher.user.domain.model.UserAccessTokenPayload
import club.staircrusher.user.domain.exception.UserAuthenticationException
import club.staircrusher.stdlib.token.TokenVerificationException
import club.staircrusher.user.domain.model.UserAuthInfo

@Component
class UserAuthService(
    private val tokenManager: TokenManager,
) {
    @Deprecated("Authentication의 책임은 User에서 UserAuthInfo로 옮겨감")
    fun issueAccessToken(user: User): String {
        return tokenManager.issueToken(UserAccessTokenPayload(userId = user.id))
    }

    fun issueAccessToken(userAuthInfo: UserAuthInfo): String {
        return tokenManager.issueToken(UserAccessTokenPayload(userId = userAuthInfo.userId))
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
