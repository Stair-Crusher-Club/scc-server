package club.staircrusher.spring_web.app

import club.staircrusher.spring_web.SccSecurityConfig.Companion.accessTokenHeader
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SccAppAccessTokenFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val accessToken = request.getHeader(accessTokenHeader)
        if (accessToken != null) {
            SecurityContextHolder.getContext().authentication = SccAppAuthentication(accessToken = accessToken)
        }
        filterChain.doFilter(request, response)
    }
}