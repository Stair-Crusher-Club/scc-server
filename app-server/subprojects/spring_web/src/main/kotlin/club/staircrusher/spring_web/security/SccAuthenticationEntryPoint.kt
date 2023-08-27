package club.staircrusher.spring_web.security

import club.staircrusher.api.spec.dto.ApiErrorResponse
import club.staircrusher.user.domain.exception.UserAuthenticationException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class SccAuthenticationEntryPoint : AuthenticationEntryPoint {
    private val objectMapper = jacksonObjectMapper()

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException?
    ) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.writer.write(
            objectMapper.writeValueAsString(
                ApiErrorResponse(
                    msg = when (authException?.cause) {
                        is UserAuthenticationException -> "로그인 정보가 유효하지 않습니다. 다시 로그인해주세요."
                        else -> authException?.message ?: ""
                    },
                    code = ApiErrorResponse.Code.UNAUTHORIZED
                )
            )
        )
    }
}
