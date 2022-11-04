package club.staircrusher.spring_web.security.admin

import club.staircrusher.spring_web.security.BeforeAuthSccAuthentication
import club.staircrusher.stdlib.token.TokenVerificationException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class SccAdminAuthenticationProvider(
    private val adminAuthenticationService: AdminAuthenticationService,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val beforeAuthSccAuthentication = authentication as BeforeAuthSccAuthentication
        val accessToken = beforeAuthSccAuthentication.credentials
        try {
            adminAuthenticationService.verifyAccessToken(accessToken)
        } catch (e: TokenVerificationException) {
            throw BadCredentialsException("Invalid access token.", e)
        }

        return SccAdminAuthentication()
    }

    override fun supports(authentication: Class<*>): Boolean {
        return BeforeAuthSccAuthentication::class.java.isAssignableFrom(authentication)
    }
}
