package club.staircrusher.user.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Component
class UserBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = emptyList<RequestMatcher>()

    override fun identifiedUserOnlyRequestMatchers() = listOf(
        "/getUserInfo",
        "/updateUserInfo",
        "/updatePushToken",
        "/deleteUser",
        "/validateUserProfile",
    ).map { AntPathRequestMatcher(it) }
}
