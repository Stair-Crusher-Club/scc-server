package club.staircrusher.accessibility.infra.adapter.`in`.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BlurFacesInPlaceAccessibilityImagesParams(
    @field:JsonProperty("placeAccessibilityId")
    val placeAccessibilityId: String
)
