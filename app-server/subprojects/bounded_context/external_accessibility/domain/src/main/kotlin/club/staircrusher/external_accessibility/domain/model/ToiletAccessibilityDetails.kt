package club.staircrusher.external_accessibility.domain.model

import jakarta.persistence.Embeddable

@Embeddable
data class ToiletAccessibilityDetails(
    val imageUrl: String? = null,
    val gender: String? = null,
    val accessDesc: String? = null,
    val availableDesc: String? = null,
    val entranceDesc: String? = null,
    val stallWidth: String? = null,
    val stallDepth: String? = null,
    val doorDesc: String? = null,
    val doorSideRoom: String? = null,
    val washStandBelowRoom: String? = null,
    val washStandHandle: String? = null,
    val extraDesc: String? = null,
)
