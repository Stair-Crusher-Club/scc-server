package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.place_search.domain.model.Place
import club.staircrusher.place_search.application.port.out.web.AccessibilityService
import club.staircrusher.place_search.application.port.out.web.BuildingService
import club.staircrusher.place_search.application.port.out.web.PlaceService
import club.staircrusher.place_search.domain.model.BuildingAccessibility
import club.staircrusher.place_search.domain.model.PlaceAccessibility
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.di.annotation.Component

@Component
class PlaceSearchService(
    private val placeService: PlaceService,
    private val buildingService: BuildingService,
    private val accessibilityService: AccessibilityService,
) {
    data class SearchPlacesResult(
        val place: Place,
        val buildingAccessibility: BuildingAccessibility?,
        val placeAccessibility: PlaceAccessibility?,
        val distanceMeters: Length? = null,
    )

    @Suppress("UnusedPrivateMember")
    suspend fun searchPlaces(
        searchText: String,
        currentLocation: Location?,
        distanceMetersLimit: Length,
        siGunGuId: String?,
        eupMyeonDongId: String?,
    ) : List<SearchPlacesResult> {
        val places = placeService.findByKeyword(searchText) // TODO: 옵션 적용
        return places.map { it.toSearchPlacesResult() }
    }

    suspend fun listPlacesInBuilding(buildingId: String): List<SearchPlacesResult> {
        val buildingAddress = buildingService.getById(buildingId)?.address
            ?: throw IllegalArgumentException("Building of id $buildingId does not exist.")
        val places = placeService.findAllByKeyword(buildingAddress)
        return places.map { it.toSearchPlacesResult() }
    }

    private fun Place.toSearchPlacesResult(): SearchPlacesResult {
        val (placeAccessibility, buildingAccessibility) = accessibilityService.getAccessibility(this)
        return SearchPlacesResult(
            place = this,
            buildingAccessibility = buildingAccessibility,
            placeAccessibility = placeAccessibility,
        )
    }
}
