package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place_search.domain.model.Place
import club.staircrusher.place_search.application.port.out.web.AccessibilityService
import club.staircrusher.place_search.application.port.out.web.BuildingService
import club.staircrusher.place_search.application.port.out.web.PlaceService
import club.staircrusher.place_search.domain.model.BuildingAccessibility
import club.staircrusher.place_search.domain.model.PlaceAccessibility
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.LocationUtils

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
        val distance: Length? = null,
        val accessibilityRegistrable: Boolean,
    )

    @Suppress("UnusedPrivateMember")
    suspend fun searchPlaces(
        searchText: String,
        currentLocation: Location?,
        distanceMetersLimit: Length,
        siGunGuId: String?,
        eupMyeonDongId: String?,
    ) : List<SearchPlacesResult> {
        val places = placeService.findAllByKeyword(
            searchText,
            option = MapsService.SearchByKeywordOption(
                region = currentLocation?.let {
                    MapsService.SearchByKeywordOption.CircleRegion(
                        centerLocation = it,
                        radiusMeters = distanceMetersLimit.meter.toInt(),
                    )
                }
            ),
        )
        return places.map { it.toSearchPlacesResult(currentLocation) }
    }

    suspend fun listPlacesInBuilding(buildingId: String): List<SearchPlacesResult> {
        val buildingAddress = buildingService.getById(buildingId)?.address
            ?: throw IllegalArgumentException("Building of id $buildingId does not exist.")
        val placesBySearch = placeService.findAllByKeyword(buildingAddress, MapsService.SearchByKeywordOption())
        val placesInPersistence = placeService.findByBuildingId(buildingId)
        return (placesBySearch + placesInPersistence)
            .removeDuplicates()
            .map { it.toSearchPlacesResult(currentLocation = null) }
    }

    private fun Place.toSearchPlacesResult(currentLocation: Location?): SearchPlacesResult {
        val (placeAccessibility, buildingAccessibility) = accessibilityService.getAccessibility(this)
        return SearchPlacesResult(
            place = this,
            buildingAccessibility = buildingAccessibility,
            placeAccessibility = placeAccessibility,
            distance = currentLocation?.let { LocationUtils.calculateDistance(it, location) },
            accessibilityRegistrable = accessibilityService.isAccessibilityRegistrable(this),
        )
    }

    private fun List<Place>.removeDuplicates(): List<Place> {
        return associateBy { it.id }.values.toList()
    }
}
