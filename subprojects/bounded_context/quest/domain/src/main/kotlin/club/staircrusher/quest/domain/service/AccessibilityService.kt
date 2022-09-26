package club.staircrusher.quest.domain.service

interface AccessibilityService {
    fun filterAccessibilityExistingPlaceIds(placeIds: List<String>): List<String>
}
