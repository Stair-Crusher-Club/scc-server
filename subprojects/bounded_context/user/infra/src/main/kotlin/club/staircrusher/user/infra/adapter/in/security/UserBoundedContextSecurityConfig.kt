package club.staircrusher.user.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Component
class UserBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf("/updateUserInfo").map { AntPathRequestMatcher(it) }
}
