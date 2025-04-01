package club.staircrusher.external_accessibility.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

class ExternalAccessibilityBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf(
        "/searchExternalAccessibilities",
        "/getExternalAccessibility",
    ).map { AntPathRequestMatcher(it) }

    override fun identifiedUserOnlyRequestMatchers() = listOf(
        "/admin/syncWithDataSource"
    ).map { AntPathRequestMatcher(it) }
}
