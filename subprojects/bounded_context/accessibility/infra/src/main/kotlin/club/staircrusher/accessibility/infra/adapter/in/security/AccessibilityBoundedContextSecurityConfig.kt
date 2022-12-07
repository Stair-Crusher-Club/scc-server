package club.staircrusher.accessibility.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Component
class AccessibilityBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf(
        "/giveBuildingAccessibilityUpvote",
        "/cancelBuildingAccessibilityUpvote",
    ).map { AntPathRequestMatcher(it) }
}
