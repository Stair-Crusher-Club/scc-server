package club.staircrusher.spring_web.security.app

import club.staircrusher.spring_web.security.BeforeAuthSccAuthentication
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.`in`.UserAuthApplicationService
import club.staircrusher.user.domain.model.UserProfile
import club.staircrusher.user.domain.exception.UserAuthenticationException
import club.staircrusher.user.domain.model.UserAccountType
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

        val user: UserProfile = userApplicationService.getUserProfile(userId)
            ?: throw BadCredentialsException("No User found with given credentials.")
        if (user.isDeleted) {
            throw BadCredentialsException("No User found with given credentials.")
        }

        val userAccount = userApplicationService.getUser(userId)
        val type = userAccount?.accountType?.name ?: UserAccountType.IDENTIFIED.name

        return SccAppAuthentication(
            AuthUser(
                id = user.id,
                type = type,
                nickname = user.nickname,
                instagramId = user.instagramId,
            ),
        )
    }

    override fun supports(authentication: Class<*>): Boolean {
        return BeforeAuthSccAuthentication::class.java.isAssignableFrom(authentication)
    }
}
