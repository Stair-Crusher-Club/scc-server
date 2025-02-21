package club.staircrusher.place_search.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Component
class PlaceSearchBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = emptyList<RequestMatcher>()

    override fun identifiedUserOnlyRequestMatchers() = listOf(
        "/listConqueredPlaces",
    ).map { AntPathRequestMatcher(it) }
}
