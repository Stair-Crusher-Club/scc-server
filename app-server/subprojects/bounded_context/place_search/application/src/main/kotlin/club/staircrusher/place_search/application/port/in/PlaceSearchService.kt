package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.place.application.port.`in`.BuildingService
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Length
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.geography.LocationUtils
import club.staircrusher.stdlib.place.PlaceCategory

@Component
class PlaceSearchService(
    private val placeService: PlaceService,
    private val buildingService: BuildingService,
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    data class SearchPlacesResult(
        val place: Place,
        val buildingAccessibility: BuildingAccessibility?,
        val placeAccessibility: PlaceAccessibility?,
        val distance: Length? = null,
        val isAccessibilityRegistrable: Boolean,
    )

    @Suppress("UnusedPrivateMember")
    suspend fun searchPlaces(
        searchText: String,
        currentLocation: Location?,
        distanceMetersLimit: Length,
        siGunGuId: String?,
        eupMyeonDongId: String?,
    ): List<SearchPlacesResult> {
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
        val buildingAddress = buildingService.getById(buildingId)?.address?.toString()
            ?: throw IllegalArgumentException("Building of id $buildingId does not exist.")
        val placesBySearch = placeService.findAllByKeyword(buildingAddress, MapsService.SearchByKeywordOption())
        val placesInPersistence = placeService.findByBuildingId(buildingId)
        return (placesBySearch + placesInPersistence)
            .removeDuplicates()
            .map { it.toSearchPlacesResult(currentLocation = null) }
    }

    private fun Place.toSearchPlacesResult(currentLocation: Location?): SearchPlacesResult {
        val placeAccessibility = accessibilityApplicationService.getPlaceAccessibility(id)
        val buildingAccessibility = accessibilityApplicationService.getBuildingAccessibility(id)
        return SearchPlacesResult(
            place = this,
            buildingAccessibility = buildingAccessibility,
            placeAccessibility = placeAccessibility,
            distance = currentLocation?.let { LocationUtils.calculateDistance(it, location) },
            isAccessibilityRegistrable = accessibilityApplicationService.isAccessibilityRegistrable(id),
        )
    }

    private fun List<Place>.removeDuplicates(): List<Place> {
        return associateBy { it.id }.values.toList()
    }
}
