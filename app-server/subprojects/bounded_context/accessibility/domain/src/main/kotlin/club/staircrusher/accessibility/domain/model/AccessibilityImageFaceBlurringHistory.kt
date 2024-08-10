package club.staircrusher.accessibility.domain.model

import java.time.Instant

data class AccessibilityImageFaceBlurringHistory(
    val id: String,
    val placeAccessibilityId: String?,
    val buildingAccessibilityId: String?,
    val originalImageUrls: List<String>,
    val blurredImageUrls: List<String>,
    val detectedPeopleCounts: List<Int>,
    val createdAt: Instant,
    val updatedAt: Instant
)
