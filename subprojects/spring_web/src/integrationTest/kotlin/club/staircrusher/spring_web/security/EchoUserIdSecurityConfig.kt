package club.staircrusher.spring_web.security

import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.stereotype.Component

@Component
class EchoUserIdSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf("/echoUserId/secured").map { AntPathRequestMatcher(it) }
}
