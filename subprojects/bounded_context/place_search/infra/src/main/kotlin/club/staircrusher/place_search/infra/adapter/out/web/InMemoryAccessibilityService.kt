package club.staircrusher.place_search.infra.adapter.out.web

import club.staircrusher.accessibility.application.AccessibilityApplicationService
import club.staircrusher.place_search.domain.model.BuildingAccessibility
import club.staircrusher.place_search.domain.model.Place
import club.staircrusher.place_search.domain.model.PlaceAccessibility
import club.staircrusher.place_search.application.port.out.web.AccessibilityService
import org.springframework.stereotype.Component

@Component
class InMemoryAccessibilityService(
    private val accessibilityApplicationService: AccessibilityApplicationService,
) : AccessibilityService {
    override fun getAccessibility(place: Place): Pair<PlaceAccessibility?, BuildingAccessibility?> {
        val accessibility = accessibilityApplicationService.getAccessibility(place.id)

        val placeAccessibility = accessibility.placeAccessibility?.let { PlaceAccessibility() }
        val buildingAccessibility = accessibility.buildingAccessibility?.let { BuildingAccessibility() }
        return Pair(placeAccessibility, buildingAccessibility)
    }
}