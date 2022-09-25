package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.accessibility.application.AccessibilityApplicationService
import club.staircrusher.quest.domain.service.AccessibilityService
import org.springframework.stereotype.Component

@Component
class InMemoryAccessibilityService(
    private val accessibilityApplicationService: AccessibilityApplicationService,
) : AccessibilityService {
    override fun filterAccessibilityExistingPlaceIds(placeIds: List<String>): List<String> {
        return accessibilityApplicationService.filterAccessibilityExistingPlaceIds(placeIds)
    }
}