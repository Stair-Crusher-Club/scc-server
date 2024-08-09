package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface AccessibilityImageFaceBlurringHistoryRepository :
    EntityRepository<AccessibilityImageFaceBlurringHistory, String> {
    fun findLatestPlaceHistoryOrNull(): AccessibilityImageFaceBlurringHistory?
    fun findLatestBuildingHistoryOrNull(): AccessibilityImageFaceBlurringHistory?
    fun findByPlaceAccessibilityId(placeAccessibilityId: String): List<AccessibilityImageFaceBlurringHistory>
    fun findByBuildingAccessibilityId(buildingAccessibilityId: String): List<AccessibilityImageFaceBlurringHistory>
    fun findAll(): List<AccessibilityImageFaceBlurringHistory>
}
