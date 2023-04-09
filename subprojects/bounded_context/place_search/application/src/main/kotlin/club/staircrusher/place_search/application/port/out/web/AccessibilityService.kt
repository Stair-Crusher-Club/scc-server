package club.staircrusher.place_search.application.port.out.web

import club.staircrusher.place_search.domain.model.BuildingAccessibility
import club.staircrusher.place_search.domain.model.Place
import club.staircrusher.place_search.domain.model.PlaceAccessibility

interface AccessibilityService {
    fun getAccessibility(place: Place): Pair<PlaceAccessibility?, BuildingAccessibility?>
    fun getByUserId(userId: String): Pair<List<PlaceAccessibility>, List<BuildingAccessibility>>
    fun isAccessibilityRegistrable(place: Place): Boolean
}
