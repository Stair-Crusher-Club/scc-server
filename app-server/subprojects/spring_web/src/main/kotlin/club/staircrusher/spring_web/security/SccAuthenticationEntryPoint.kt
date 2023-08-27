package club.staircrusher.spring_web.security

import club.staircrusher.user.domain.exception.UserAuthenticationException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class SccAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        when (authException?.cause) {
            is UserAuthenticationException -> {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.")
            }
            else -> {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), authException?.message ?: "")
            }
        }
    }
}
