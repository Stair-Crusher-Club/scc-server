package club.staircrusher.spring_web.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler

/**
 * 인증이 성공했을 때의 동작을 결정하는 AuthenticationSuccessHandler의 기본값은 SavedRequestAwareAuthenticationSuccessHandler이다.
 * 근데 SavedRequestAwareAuthenticationSuccessHandler는 인증이 성공하면 root path로 redirect를 시켜버린다.
 * 즉, 인증이 성공하는 경우 root path로 redirect하는 응답이 먼저 씌여진 후 controller 로직이 실행되는데,
 * 이때 Spring MVC은 이미 응답이 쓰여져 있으므로 controller 로직의 결과물을 무시하고 무조건 root path로 redirect 시킨다.
 *
 * 이를 방지하기 위해 인증 성공 시 아무것도 안 하는 AuthenticationSuccessHandler를 사용한다.
 * @see [SccAuthenticationFilter]
 */
class SccAuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {
    override fun handle(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        // No-op
    }
}
