package club.staircrusher.spring_web.security

import club.staircrusher.spring_web.persistence.LocalCacheBuilder
import club.staircrusher.spring_web.security.admin.AdminAuthenticationService
import club.staircrusher.spring_web.security.admin.SccAdminAuthenticationProvider
import club.staircrusher.spring_web.security.app.SccAppAuthenticationProvider
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.`in`.UserAuthApplicationService
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationFilter

@Component
class SccAuthenticationFilter(
    userAuthApplicationService: UserAuthApplicationService,
    userApplicationService: UserApplicationService,
    adminAuthenticationService: AdminAuthenticationService,
    transactionManager: TransactionManager,
    localCacheBuilder: LocalCacheBuilder,
) : AuthenticationFilter(
    ProviderManager(
        SccAppAuthenticationProvider(
            userAuthApplicationService,
            userApplicationService,
            transactionManager,
            localCacheBuilder,
        ),
        SccAdminAuthenticationProvider(adminAuthenticationService),
    ),
    AuthenticationConverter { request ->
        val accessToken = request.getHeader(HttpHeaders.AUTHORIZATION)?.replace("Bearer ", "")
        accessToken?.let { BeforeAuthSccAuthentication(it) }
    }
) {
    init {
        successHandler = SccAuthenticationSuccessHandler()
    }
}
