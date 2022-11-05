package club.staircrusher.spring_web.security.admin

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.token.TokenManager
import club.staircrusher.stdlib.token.TokenVerificationException
import org.springframework.beans.factory.annotation.Value

@Component
class AdminAuthenticationService(
    private val tokenManager: TokenManager,
    @Value("\${scc.admin.password}") private val adminPassword: String,
) {
    /**
     * @return access token.
     */
    fun login(username: String, password: String): String {
        if (username != ADMIN_USERNAME) {
            throw AdminAuthenticationException("${username}은 잘못된 계정입니다.")
        }
        if (password != adminPassword) {
            throw AdminAuthenticationException("잘못된 패스워드입니다.")
        }
        return tokenManager.issueToken(ADMIN_USERNAME)
    }

    @Throws(AdminAuthenticationException::class)
    fun verifyAccessToken(token: String) {
        val verifiedTokenContent = try {
            tokenManager.verify(token, String::class)
        } catch (_: TokenVerificationException) {
            throw AdminAuthenticationException("잘못된 인증 정보입니다. 다시 로그인해주세요.")
        }
        if (verifiedTokenContent != ADMIN_USERNAME) {
            throw AdminAuthenticationException("잘못된 인증 정보입니다. 다시 로그인해주세요.")
        }
    }

    companion object {
        const val ADMIN_USERNAME = "admin"
    }
}
