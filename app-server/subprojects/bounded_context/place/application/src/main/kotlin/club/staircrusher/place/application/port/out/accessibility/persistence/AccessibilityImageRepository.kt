package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import org.springframework.data.repository.CrudRepository

interface AccessibilityImageRepository : CrudRepository<AccessibilityImage, String> {
    fun findByAccessibilityIdAndAccessibilityType(accessibilityId: String, accessibilityType: AccessibilityImage.AccessibilityType): List<AccessibilityImage>
}
