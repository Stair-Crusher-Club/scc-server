package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.AccessibilityImageFaceBlurringHistory
import org.springframework.data.repository.CrudRepository

interface AccessibilityImageFaceBlurringHistoryRepository : CrudRepository<AccessibilityImageFaceBlurringHistory, String> {
    fun findTop5ByPlaceAccessibilityIdIsNotNullOrderByCreatedAtDesc(): List<AccessibilityImageFaceBlurringHistory>
    fun findTop5ByBuildingAccessibilityIdIsNotNullOrderByCreatedAtDesc(): List<AccessibilityImageFaceBlurringHistory>
    fun findByPlaceAccessibilityId(placeAccessibilityId: String): List<AccessibilityImageFaceBlurringHistory>
    fun findByPlaceAccessibilityIdIn(placeAccessibilityIds: Collection<String>): List<AccessibilityImageFaceBlurringHistory>
    fun findByBuildingAccessibilityId(buildingAccessibilityId: String): List<AccessibilityImageFaceBlurringHistory>
    fun findByBuildingAccessibilityIdIn(buildingAccessibilityIds: Collection<String>): List<AccessibilityImageFaceBlurringHistory>
}
