package club.staircrusher.place.domain.model.accessibility

import jakarta.persistence.Embeddable

@Embeddable
data class AccessibilityImage(
    val imageUrl: String,
    var thumbnailUrl: String? = null,
)
