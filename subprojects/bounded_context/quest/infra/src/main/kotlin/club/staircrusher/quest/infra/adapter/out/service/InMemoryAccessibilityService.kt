package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.accessibility.application.AccessibilityApplicationService
import club.staircrusher.quest.domain.service.AccessibilityService
import club.staircrusher.stdlib.di.annotation.Component

/**
 * Add suffix 2 in order to avoid conflicts with another InMemoryAccessibilityService
 */
@Component
class InMemoryAccessibilityService2(
    private val accessibilityApplicationService: AccessibilityApplicationService,
) : AccessibilityService {
    override fun filterAccessibilityExistingPlaceIds(placeIds: List<String>): List<String> {
        return accessibilityApplicationService.filterAccessibilityExistingPlaceIds(placeIds)
    }
}