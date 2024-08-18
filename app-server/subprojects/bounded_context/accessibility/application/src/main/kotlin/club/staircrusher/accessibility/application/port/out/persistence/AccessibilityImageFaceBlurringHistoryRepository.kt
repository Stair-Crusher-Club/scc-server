package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import org.springframework.data.repository.CrudRepository

interface AccessibilityImageFaceBlurringHistoryRepository : CrudRepository<AccessibilityImageFaceBlurringHistory, String> {
    fun findFirstByPlaceAccessibilityIdIsNotNullOrderByCreatedAtDesc(): AccessibilityImageFaceBlurringHistory?
    fun findFirstByBuildingAccessibilityIdIsNotNullOrderByCreatedAtDesc(): AccessibilityImageFaceBlurringHistory?
    fun findByPlaceAccessibilityId(placeAccessibilityId: String): List<AccessibilityImageFaceBlurringHistory>
    fun findByBuildingAccessibilityId(buildingAccessibilityId: String): List<AccessibilityImageFaceBlurringHistory>
}
