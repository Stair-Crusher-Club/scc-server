package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.quest.application.port.out.web.AccessibilityService
import club.staircrusher.stdlib.di.annotation.Component

/**
 * FIXME: delete suffix 2 avoiding conflicts with another InMemoryAccessibilityService
 */
@Component
class InMemoryAccessibilityService2(
    private val accessibilityApplicationService: AccessibilityApplicationService,
) : AccessibilityService {
    override fun filterAccessibilityExistingPlaceIds(placeIds: List<String>): List<String> {
        return accessibilityApplicationService.filterAccessibilityExistingPlaceIds(placeIds)
    }
}
