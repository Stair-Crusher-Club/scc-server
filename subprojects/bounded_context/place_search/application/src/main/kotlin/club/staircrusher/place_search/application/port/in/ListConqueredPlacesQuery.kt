package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ListConqueredPlacesQuery(
    private val transactionManager: TransactionManager,
    private val placeService: PlaceService,
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    fun listConqueredPlaces(userId: String): List<PlaceSearchService.SearchPlacesResult> = transactionManager.doInTransaction {
        val (placeAccessibilities, buildingAccessibilities) = accessibilityApplicationService.findByUserId(userId)
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
                isAccessibilityRegistrable = accessibilityApplicationService.isAccessibilityRegistrable(place.id),
            )
        }
    }
}
