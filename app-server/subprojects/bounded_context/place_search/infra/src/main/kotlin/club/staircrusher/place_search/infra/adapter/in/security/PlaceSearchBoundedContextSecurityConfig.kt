package club.staircrusher.place_search.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Component
class PlaceSearchBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf(
        "/searchPlaces",
        "/listPlacesInBuilding",
        "/getPlaceWithBuilding",
        "/listSearchKeywordsOfPlaceCategory",
    ).map { AntPathRequestMatcher(it) }

    override fun identifiedUserOnlyRequestMatchers() = listOf(
        "/listConqueredPlaces",
    ).map { AntPathRequestMatcher(it) }
}
