package club.staircrusher.accessibility.domain.model

import jakarta.persistence.Embeddable

@Embeddable
data class AccessibilityImage(
    val imageUrl: String,
    var thumbnailUrl: String? = null,
)
