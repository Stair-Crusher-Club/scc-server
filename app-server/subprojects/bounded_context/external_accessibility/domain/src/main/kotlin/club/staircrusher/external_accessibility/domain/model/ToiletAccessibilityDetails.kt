package club.staircrusher.external_accessibility.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ToiletAccessibilityDetails(
    val gender: Boolean? = null,
    val accessDesc: String? = null,
    val availableDesc: String? = null,
    val entranceDesc: String? = null,
    val stallDesc: String? = null,
    val doorDesc: String? = null,
    val washStandDesc: String? = null,
    val extra: String? = null,
)
