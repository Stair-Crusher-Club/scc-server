package club.staircrusher.spring_web.authentication

import club.staircrusher.spring_web.authentication.app.SccAppAuthenticationProvider
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.application.port.`in`.UserAuthApplicationService
import org.springframework.security.authentication.ProviderManager
import org.springframework.stereotype.Component

@Component
class SccAuthenticationManager(
    userAuthApplicationService: UserAuthApplicationService,
    userApplicationService: UserApplicationService,
    transactionManager: TransactionManager,
) : ProviderManager(
    SccAppAuthenticationProvider(
        userAuthApplicationService,
        userApplicationService,
        transactionManager,
    ),
)
