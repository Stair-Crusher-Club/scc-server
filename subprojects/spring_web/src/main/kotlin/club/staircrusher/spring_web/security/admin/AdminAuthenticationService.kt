package club.staircrusher.spring_web.security.admin

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.token.TokenManager
import club.staircrusher.stdlib.token.TokenVerificationException

@Component
class AdminAuthenticationService(
    private val tokenManager: TokenManager,
) {
    fun issueAccessToken(): String {
        return tokenManager.issueToken(accessTokenContent)
    }

    @Throws(AdminAuthenticationException::class)
    fun verifyAccessToken(token: String) {
        val verifiedTokenContent = try {
            tokenManager.verify(token, String::class)
        } catch (_: TokenVerificationException) {
            throw AdminAuthenticationException()
        }
        if (verifiedTokenContent != accessTokenContent) {
            throw AdminAuthenticationException()
        }
    }

    private val accessTokenContent = "Admin"
}
