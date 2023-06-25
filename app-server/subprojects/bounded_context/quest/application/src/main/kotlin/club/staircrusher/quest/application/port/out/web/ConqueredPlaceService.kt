package club.staircrusher.quest.application.port.out.web

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.quest.domain.model.ClubQuest
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ConqueredPlaceService(
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    fun getConqueredPlaceIds(clubQuest: ClubQuest): Set<String> {
        val placeIds = clubQuest.targetBuildings.flatMap {
            it.places.map { it.placeId }
        }
        return accessibilityApplicationService.filterAccessibilityExistingPlaceIds(placeIds).toSet()
    }
}
