package club.staircrusher.spring_web.security

import org.springframework.http.HttpHeaders
import org.springframework.security.web.authentication.AuthenticationConverter
import org.springframework.security.web.authentication.AuthenticationFilter

class SccAuthenticationFilter(
    sccAuthenticationManager: SccAuthenticationManager,
) : AuthenticationFilter(
    sccAuthenticationManager,
    AuthenticationConverter { request ->
        val accessToken = request.getHeader(HttpHeaders.AUTHORIZATION)?.replace("Bearer ", "")
        accessToken?.let { BeforeAuthSccAuthentication(it) }
    }
)
