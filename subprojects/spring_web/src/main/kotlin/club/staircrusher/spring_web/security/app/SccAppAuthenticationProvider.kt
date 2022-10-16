package club.staircrusher.spring_web.security.app

import club.staircrusher.spring_web.security.BeforeAuthSccAuthentication
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.`in`.UserAuthApplicationService
import club.staircrusher.user.domain.model.User
import club.staircrusher.user.domain.exception.TokenVerificationException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class SccAppAuthenticationProvider(
    private val userAuthApplicationService: UserAuthApplicationService,
    private val userApplicationService: UserApplicationService,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val beforeAuthSccAuthentication = authentication as BeforeAuthSccAuthentication
        val accessToken = beforeAuthSccAuthentication.credentials
        val userId = try {
            userAuthApplicationService.verify(accessToken)
        } catch (e: TokenVerificationException) {
            throw BadCredentialsException("Invalid access token.", e)
        }

        val user: User = userApplicationService.getUser(userId)
            ?: throw BadCredentialsException("No User found with given credentials.")

        return SccAppAuthentication(
            AuthUser(
                id = user.id,
                nickname = user.nickname,
                instagramId = user.instagramId,
            ),
        )
    }

    override fun supports(authentication: Class<*>): Boolean {
        return BeforeAuthSccAuthentication::class.java.isAssignableFrom(authentication)
    }
}
