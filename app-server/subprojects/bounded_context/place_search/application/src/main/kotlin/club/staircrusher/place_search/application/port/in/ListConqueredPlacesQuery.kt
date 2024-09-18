package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.accessibility.domain.model.AccessibilityScore
import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ListConqueredPlacesQuery(
    private val transactionManager: TransactionManager,
    private val placeApplicationService: PlaceApplicationService,
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    fun listConqueredPlaces(userId: String): List<PlaceSearchService.SearchPlacesResult> = transactionManager.doInTransaction {
        val (placeAccessibilities, buildingAccessibilities) = accessibilityApplicationService.findByUserId(userId)
        val placeAccessibilityByPlaceId = placeAccessibilities.associateBy { it.placeId }
        val buildingAccessibilityByBuildingId = buildingAccessibilities.associateBy { it.buildingId }
        val placeById = placeApplicationService.findAllByIds(placeAccessibilityByPlaceId.keys)
            .associateBy { it.id }
        val placeIdToIsFavoriteMap = placeApplicationService.isFavoritePlaces(userId = userId, placeIds = placeAccessibilityByPlaceId.keys)

        placeAccessibilityByPlaceId.map { (placeId, placeAccessibility) ->
            val place = placeById[placeId]!!
            val ba = buildingAccessibilityByBuildingId[place.building.id]
            PlaceSearchService.SearchPlacesResult(
                place = place,
                placeAccessibility = placeAccessibility,
                buildingAccessibility = ba,
                distance = null,
                accessibilityScore = AccessibilityScore.get(placeAccessibility, ba),
                isAccessibilityRegistrable = accessibilityApplicationService.isAccessibilityRegistrable(place.building),
                isFavoritePlace = placeIdToIsFavoriteMap[placeId] ?: false
            )
        }
            .sortedByDescending { it.placeAccessibility?.createdAt }
    }
}
