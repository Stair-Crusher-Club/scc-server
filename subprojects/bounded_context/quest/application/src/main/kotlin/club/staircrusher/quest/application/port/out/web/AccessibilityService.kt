package club.staircrusher.quest.application.port.out.web

interface AccessibilityService {
    fun filterAccessibilityExistingPlaceIds(placeIds: List<String>): List<String>
}
