package club.staircrusher.place.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

class PlaceBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = emptyList<RequestMatcher>()

    override fun identifiedUserOnlyRequestMatchers() = listOf(
        "/createPlaceFavorite",
        "/deletePlaceFavorite",
        "/listPlaceFavoritesByUser",
    ).map { AntPathRequestMatcher(it) }
}
