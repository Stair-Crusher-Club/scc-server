package club.staircrusher.spring_web.app

import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.user.UserApplicationService
import club.staircrusher.user.application.user.UserAuthApplicationService
import club.staircrusher.user.domain.service.TokenVerificationException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication

class SccAppAuthenticationProvider(
    private val userAuthApplicationService: UserAuthApplicationService,
    private val userApplicationService: UserApplicationService,
    private val transactionManager: TransactionManager,
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication): Authentication {
        val sccAppAuthentication = authentication as SccAppAuthentication
        if (authentication.isAuthenticated) {
            return authentication
        }

        val accessToken = sccAppAuthentication.credentials ?: throw AuthenticationCredentialsNotFoundException("Access token does not exist.")
        val userId = try {
            userAuthApplicationService.verify(accessToken)
        } catch (e: TokenVerificationException) {
            throw BadCredentialsException("Invalid access token.")
        }

        val user = transactionManager.doInTransaction {
            userApplicationService.getUser(userId)
        } ?: throw BadCredentialsException("No User found with given credentials.")
        sccAppAuthentication.setUserInfo(SccAppAuthentication.UserDetail(
            userId = user.id,
            nickname = user.nickname,
            instagramId = user.instagramId,
        ))
        return sccAppAuthentication
    }

    override fun supports(authentication: Class<*>): Boolean {
        return SccAppAuthentication::class.java.isAssignableFrom(authentication)
    }
}
