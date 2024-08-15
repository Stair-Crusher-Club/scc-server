package club.staircrusher.accessibility.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Component
class AccessibilityBoundedContextSecurityConfig : SccSecurityConfig {
    // TODO: 뭔가 불편하고 error prone, annotation으로 해결할 수 없을까?
    override fun requestMatchers() = listOf(
        "/giveBuildingAccessibilityUpvote",
        "/cancelBuildingAccessibilityUpvote",
        "/deleteAccessibility",
        "/getAccessibilityRank",
        "/getCountForNextRank",
        "/registerPlaceAccessibility",
        "/registerBuildingAccessibility",
        "/admin/accessibilityAllowedRegions"
    ).map { AntPathRequestMatcher(it) }
}
