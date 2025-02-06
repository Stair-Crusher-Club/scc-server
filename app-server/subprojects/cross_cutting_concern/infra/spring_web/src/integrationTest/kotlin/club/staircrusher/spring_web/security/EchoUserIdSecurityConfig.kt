package club.staircrusher.spring_web.security

import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import org.springframework.stereotype.Component

@Component
class EchoUserIdSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf("/echoUserId/secured").map { AntPathRequestMatcher(it) }
    override fun identifiedUserOnlyRequestMatchers() = listOf("/echoUserId/identified").map { AntPathRequestMatcher(it) }
}
