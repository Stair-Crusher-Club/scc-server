package club.staircrusher.place_search.infra.adapter.out.web

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.place_search.domain.model.BuildingAccessibility
import club.staircrusher.place_search.domain.model.Place
import club.staircrusher.place_search.domain.model.PlaceAccessibility
import club.staircrusher.place_search.application.port.out.web.AccessibilityService
import club.staircrusher.stdlib.di.annotation.Component

@Component
class InMemoryAccessibilityService(
    private val accessibilityApplicationService: AccessibilityApplicationService,
) : AccessibilityService {
    override fun getAccessibility(place: Place): Pair<PlaceAccessibility?, BuildingAccessibility?> {
        val placeAccessibility = accessibilityApplicationService.getPlaceAccessibility(place.id)?.toModel()
        val buildingAccessibility = accessibilityApplicationService.getBuildingAccessibility(place.id)?.toModel()
        return Pair(placeAccessibility, buildingAccessibility)
    }

    override fun getByUserId(userId: String): Pair<List<PlaceAccessibility>, List<BuildingAccessibility>> {
        val (placeAccessibilities, buildingAccessibilities) = accessibilityApplicationService.findByUserId(userId)
        return Pair(
            placeAccessibilities.map { it.toModel() },
            buildingAccessibilities.map { it.toModel() },
        )
    }

    private fun club.staircrusher.accessibility.domain.model.PlaceAccessibility.toModel() = PlaceAccessibility(
        id = id,
        placeId = placeId,
    )

    private fun club.staircrusher.accessibility.domain.model.BuildingAccessibility.toModel() = BuildingAccessibility(
        id = id,
        buildingId = buildingId,
    )
}
