package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory
import java.time.Instant

@Component
class ListNearbyAccessiblePlacesUseCase(
    private val placeSearchService: PlaceSearchService,
) {
    suspend fun handle(placeCategory: PlaceCategory, currentLocation: Location): List<PlaceSearchService.SearchPlacesResult> {
        val placeSearchResult = placeSearchService.listNearbyPlacesByCategory(placeCategory, currentLocation, SEARCH_RADIUS_METERS)

        if (placeSearchResult.size < MINIMUM_PLACE_COUNT) return emptyList()
        return placeSearchResult
            .filter { it.accessibilityScore != null && it.accessibilityScore in (ACCESSIBILITY_SCORE_CRITERIA_START..ACCESSIBILITY_SCORE_CRITERIA_END_INCLUSIVE) }
            .sortedWith(sortCriteria)
            .take(TOTAL_LIMIT)
    }

    private val sortCriteria = compareBy<PlaceSearchService.SearchPlacesResult> { it.distance?.meter ?: Double.MAX_VALUE }
        .thenByDescending { it.placeAccessibility?.createdAt ?: Instant.MIN }

    companion object {
        private const val SEARCH_RADIUS_METERS = 500
        private const val MINIMUM_PLACE_COUNT = 3
        private const val ACCESSIBILITY_SCORE_CRITERIA_START = 0.0
        private const val ACCESSIBILITY_SCORE_CRITERIA_END_INCLUSIVE = 2.0
        private const val TOTAL_LIMIT = 30
    }
}
