package club.staircrusher.spring_web.app

import club.staircrusher.user.domain.service.TokenVerificationException
import club.staircrusher.user.domain.service.UserAuthService
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication

class SccAppAuthenticationProvider(
    private val userAuthService: UserAuthService,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val sccAppAuthentication = authentication as SccAppAuthentication
        if (authentication.isAuthenticated) {
            return authentication
        }

        val accessToken = sccAppAuthentication.credentials ?: throw AuthenticationCredentialsNotFoundException("Access token does not exist.")
        val tokenPayload = try {
            userAuthService.verifyAccessToken(accessToken)
        } catch (e: TokenVerificationException) {
            throw BadCredentialsException("Invalid access token.")
        }

        sccAppAuthentication.setUserInfo(tokenPayload.userId)
        return sccAppAuthentication
    }

    override fun supports(authentication: Class<*>): Boolean {
        return SccAppAuthentication::class.java.isAssignableFrom(authentication)
    }
}
