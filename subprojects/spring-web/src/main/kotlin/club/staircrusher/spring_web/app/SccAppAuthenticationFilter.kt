package club.staircrusher.spring_web.app

import club.staircrusher.spring_web.SccSecurityConfig
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationFilter
import org.springframework.stereotype.Component

@Component
class SccAppAuthenticationFilter(
    sccAppAuthenticationManager: SccAppAuthenticationManager,
) : AuthenticationFilter(
    sccAppAuthenticationManager,
    AuthenticationConverter { request ->
        val accessToken = request.getHeader(SccSecurityConfig.accessTokenHeader)
            ?: throw AuthenticationCredentialsNotFoundException("Access token does not exists.")
        SccAppAuthentication(accessToken = accessToken)
    },
)
