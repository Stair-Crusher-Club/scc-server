package club.staircrusher.spring_web.authentication

import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationFilter

class SccAuthenticationFilter(
    sccAuthenticationManager: SccAuthenticationManager,
) : AuthenticationFilter(
    sccAuthenticationManager,
    AuthenticationConverter { request ->
        val accessToken = request.getHeader(SccSecurityFilterChainConfig.accessTokenHeader)
        accessToken?.let { BeforeAuthSccAuthentication(it) }
    }
)
