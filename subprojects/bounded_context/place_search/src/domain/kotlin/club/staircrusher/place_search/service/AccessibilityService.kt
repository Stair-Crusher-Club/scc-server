package club.staircrusher.place_search.service

import club.staircrusher.place_search.model.BuildingAccessibility
import club.staircrusher.place_search.model.Place
import club.staircrusher.place_search.model.PlaceAccessibility

interface AccessibilityService {
    fun getAccessibility(place: Place): Pair<PlaceAccessibility?, BuildingAccessibility?>
}