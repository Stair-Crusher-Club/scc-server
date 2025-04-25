package club.staircrusher.place.infra.adapter.`in`.security

import club.staircrusher.spring_web.security.SccSecurityConfig
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Component
class PlaceBoundedContextSecurityConfig : SccSecurityConfig {
    override fun requestMatchers() = listOf(
        // TODO: 뭔가 불편하고 error prone, annotation으로 해결할 수 없을까?
        "/searchPlaces",
        "/listPlacesInBuilding",
        "/getPlaceWithBuilding",
        "/listSearchKeywordsOfPlaceCategory",
        "/getAccessibilityLeaderboard",

        "/admin/accessibilityAllowedRegions",
    ).map { AntPathRequestMatcher(it) }

    override fun identifiedUserOnlyRequestMatchers() = listOf(
        "/createPlaceFavorite",
        "/deletePlaceFavorite",
        "/listPlaceFavorites",
        "/listConqueredPlaces",
        "/giveBuildingAccessibilityUpvote",
        "/cancelBuildingAccessibilityUpvote",
        "/deleteAccessibility",
        "/getAccessibilityRank",
        "/getCountForNextRank",
        "/registerPlaceAccessibility",
        "/registerBuildingAccessibility",
        "/reportAccessibility",
        "/getImageUploadUrls",
        "/getAccessibilityActivityReport",
    ).map { AntPathRequestMatcher(it) }
}
