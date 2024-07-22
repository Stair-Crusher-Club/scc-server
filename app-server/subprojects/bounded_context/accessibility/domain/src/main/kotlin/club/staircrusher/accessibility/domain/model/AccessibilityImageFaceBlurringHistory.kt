package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class AccessibilityImageFaceBlurringHistory(
    val id: String,
    val placeAccessibilityId: String?,
    val buildingAccessibilityId: String?,
    val beforeImageUrl: String?,
    val afterImageUrl: String?,
    val detectedPeopleCount: Int?,
    val createdAt: Instant,
    val updatedAt: Instant
)
