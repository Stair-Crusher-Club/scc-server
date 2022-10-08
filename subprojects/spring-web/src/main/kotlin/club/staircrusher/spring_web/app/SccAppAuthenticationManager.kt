package club.staircrusher.spring_web.app

import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.user.UserApplicationService
import club.staircrusher.user.application.user.UserAuthApplicationService
import org.springframework.security.authentication.ProviderManager
import org.springframework.stereotype.Component

@Component
class SccAppAuthenticationManager(
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
