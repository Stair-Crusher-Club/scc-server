package club.staircrusher.spring_web.security.app

import club.staircrusher.spring_web.security.BeforeAuthSccAuthentication
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.`in`.UserAuthApplicationService
import club.staircrusher.user.domain.exception.UserAuthenticationException
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
        } catch (e: UserAuthenticationException) {
            throw BadCredentialsException("Invalid access token.", e)
        }

        val user = userApplicationService.getAccountOrNull(userId)
            ?: throw BadCredentialsException("No User found with given credentials.")
        if (user.isDeleted) {
            throw BadCredentialsException("No User found with given credentials.")
        }
        val userProfile = userApplicationService.getProfileByUserIdOrNull(userId)

        return SccAppAuthentication(
            AuthUser(
                id = user.id,
                type = user.accountType.name,
                nickname = userProfile?.nickname,
                instagramId = userProfile?.instagramId,
            ),
        )
    }

    override fun supports(authentication: Class<*>): Boolean {
        return BeforeAuthSccAuthentication::class.java.isAssignableFrom(authentication)
    }
}
