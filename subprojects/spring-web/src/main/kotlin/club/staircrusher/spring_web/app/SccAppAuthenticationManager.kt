package club.staircrusher.spring_web.app

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.domain.service.UserAuthService
import org.springframework.security.authentication.ProviderManager

@Component
class SccAppAuthenticationManager(
    userAuthService: UserAuthService
) : ProviderManager(SccAppAuthenticationProvider(userAuthService))
