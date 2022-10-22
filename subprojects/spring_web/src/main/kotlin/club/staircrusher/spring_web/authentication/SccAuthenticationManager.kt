package club.staircrusher.spring_web.authentication

import club.staircrusher.spring_web.authentication.app.SccAppAuthenticationProvider
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.`in`.UserAuthApplicationService
import org.springframework.security.authentication.ProviderManager
import org.springframework.stereotype.Component

@Component
class SccAuthenticationManager(
    userAuthApplicationService: UserAuthApplicationService,
    userApplicationService: UserApplicationService,
) : ProviderManager(
    SccAppAuthenticationProvider(
        userAuthApplicationService,
        userApplicationService,
    ),
)
