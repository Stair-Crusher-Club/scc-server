package club.staircrusher.place_search.service

import club.staircrusher.place_search.model.BuildingAccessibility
import club.staircrusher.place_search.model.Place
import club.staircrusher.place_search.model.PlaceAccessibility

class InMemoryAccessibilityService : AccessibilityService {
    override fun getAccessibility(place: Place): Pair<PlaceAccessibility?, BuildingAccessibility?> {
        TODO("Not yet implemented")
    }
}