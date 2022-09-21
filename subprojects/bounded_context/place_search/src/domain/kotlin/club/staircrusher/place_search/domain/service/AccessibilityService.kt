package club.staircrusher.place_search.domain.service

import club.staircrusher.place_search.domain.model.BuildingAccessibility
import club.staircrusher.place_search.domain.model.Place
import club.staircrusher.place_search.domain.model.PlaceAccessibility

interface AccessibilityService {
    fun getAccessibility(place: Place): Pair<PlaceAccessibility?, BuildingAccessibility?>
}