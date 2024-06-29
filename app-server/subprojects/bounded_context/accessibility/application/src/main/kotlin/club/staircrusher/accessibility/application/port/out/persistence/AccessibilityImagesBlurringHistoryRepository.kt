package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityImagesBlurringHistory
import club.staircrusher.stdlib.domain.repository.EntityRepository

@Suppress("TooManyFunctions")
interface AccessibilityImagesBlurringHistoryRepository : EntityRepository<AccessibilityImagesBlurringHistory, String> {
    fun findLatestPlaceHistoryOrNull(): AccessibilityImagesBlurringHistory?
    fun findLatestBuildingHistoryOrNull(): AccessibilityImagesBlurringHistory?
    fun findByPlaceAccessibilityId(placeAccessibilityId: String): List<AccessibilityImagesBlurringHistory>
    fun findByBuildingAccessibilityId(buildingAccessibilityId: String): List<AccessibilityImagesBlurringHistory>
}
