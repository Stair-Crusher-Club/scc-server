package club.staircrusher.spring_web.security

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Component
class EchoUserIdSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf("/echoUserId/secured").map { AntPathRequestMatcher(it) }
    override fun identifiedUserOnlyRequestMatchers() = listOf("/echoUserId/identified").map { AntPathRequestMatcher(it) }
}
