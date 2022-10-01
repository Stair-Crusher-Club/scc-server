package club.staircrusher.spring_web.app

import club.staircrusher.spring_web.SccSecurityConfig
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationFilter
import org.springframework.stereotype.Component

@Component
class SccAppAuthenticationFilter(
    sccAppAuthenticationManager: SccAppAuthenticationManager,
) : AuthenticationFilter(
    sccAppAuthenticationManager,
    object : AuthenticationConverter {
        override fun convert(request: HttpServletRequest): Authentication? {
            val accessToken = request.getHeader(SccSecurityConfig.accessTokenHeader) ?: return null
            return SccAppAuthentication(accessToken = accessToken)
        }
    }
)
