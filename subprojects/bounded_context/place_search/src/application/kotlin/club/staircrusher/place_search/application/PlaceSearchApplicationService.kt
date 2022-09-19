package club.staircrusher.place_search.application

import club.staircrusher.place_search.model.Place
import club.staircrusher.place_search.service.AccessibilityService
import club.staircrusher.place_search.service.PlaceService
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location

class PlaceSearchApplicationService(
    private val placeService: PlaceService,
    private val accessibilityService: AccessibilityService,
) {
    data class SearchPlacesResult(
        val place: Place,
        val hasBuildingAccessibility: Boolean,
        val hasPlaceAccessibility: Boolean,
        val distanceMeters: Length? = null,
    )

    suspend fun searchPlaces(
        searchText: String,
        currentLocation: Location?,
        distanceMetersLimit: Length,
        siGunGuId: String,
        eupMyeonDongId: String,
    ) : List<SearchPlacesResult> {
        val places = placeService.findByKeyword(searchText)
        return places.map {
            val (placeAccessibility, buildingAccessibility) = accessibilityService.getAccessibility(it)
            SearchPlacesResult(
                place = it,
                hasBuildingAccessibility = buildingAccessibility != null,
                hasPlaceAccessibility = placeAccessibility != null,
            )
        }
    }
}