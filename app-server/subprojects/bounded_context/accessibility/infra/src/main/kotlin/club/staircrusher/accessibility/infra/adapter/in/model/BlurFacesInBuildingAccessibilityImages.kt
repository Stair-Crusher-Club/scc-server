package club.staircrusher.accessibility.infra.adapter.`in`.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BlurFacesInBuildingAccessibilityImages(
    @field:JsonProperty("placeAccessibilityId")
    val buildingAccessibilityId: String
)

