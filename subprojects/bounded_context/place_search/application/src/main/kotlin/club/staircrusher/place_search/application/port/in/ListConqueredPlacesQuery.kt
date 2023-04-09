package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.place_search.application.port.out.web.AccessibilityService
import club.staircrusher.place_search.application.port.out.web.PlaceService
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ListConqueredPlacesQuery(
    private val transactionManager: TransactionManager,
    private val placeService: PlaceService,
    private val accessibilityService: AccessibilityService,
) {
    fun listConqueredPlaces(userId: String): List<PlaceSearchService.SearchPlacesResult> = transactionManager.doInTransaction {
        val (placeAccessibilities, buildingAccessibilities) = accessibilityService.getByUserId(userId)
        val placeAccessibilityByPlaceId = placeAccessibilities.associateBy { it.placeId }
        val buildingAccessibilityByBuildingId = buildingAccessibilities.associateBy { it.buildingId }
        val placeById = placeService.findAllByIds(placeAccessibilityByPlaceId.keys)
            .associateBy { it.id }
        placeAccessibilityByPlaceId.map { (placeId, placeAccessibility) ->
            val place = placeById[placeId]!!
            PlaceSearchService.SearchPlacesResult(
                place = place,
                placeAccessibility = placeAccessibility,
                buildingAccessibility = buildingAccessibilityByBuildingId[place.building.id],
                distance = null,
                accessibilityRegistrable = accessibilityService.isAccessibilityRegistrable(place),
            )
        }
    }
}
