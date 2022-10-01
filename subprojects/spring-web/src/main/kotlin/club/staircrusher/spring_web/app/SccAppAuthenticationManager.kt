package club.staircrusher.spring_web.app

import club.staircrusher.user.domain.service.UserAuthService
import org.springframework.security.authentication.ProviderManager
import org.springframework.stereotype.Component

@Component
class SccAppAuthenticationManager(
    userAuthService: UserAuthService
) : ProviderManager(SccAppAuthenticationProvider(userAuthService))
