package club.staircrusher.spring_web.security.admin

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.token.TokenManager
import club.staircrusher.stdlib.token.TokenVerificationException

@Component
class AdminAuthenticationService(
    private val tokenManager: TokenManager,
    private val properties: AdminAuthenticationProperties,
) {
    /**
     * @return access token.
     */
    fun login(username: String, password: String): String {
        if (username != properties.username) {
            throw AdminAuthenticationException("${username}은 잘못된 계정입니다.")
        }
        if (password != properties.password) {
            throw AdminAuthenticationException("잘못된 패스워드입니다.")
        }
        return tokenManager.issueToken(properties.username)
    }

    @Throws(AdminAuthenticationException::class)
    fun verifyAccessToken(token: String) {
        val verifiedTokenContent = try {
            tokenManager.verify(token, String::class)
        } catch (_: TokenVerificationException) {
            throw AdminAuthenticationException("잘못된 인증 정보입니다. 다시 로그인해주세요.")
        }
        if (verifiedTokenContent != properties.username) {
            throw AdminAuthenticationException("잘못된 인증 정보입니다. 다시 로그인해주세요.")
        }
    }
}
