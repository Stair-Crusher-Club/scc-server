package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.BlurFacesInAccessibilityImagesUseCase
import club.staircrusher.accessibility.application.port.`in`.BlurFacesInLatestBuildingAccessibilityImagesUseCase
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityImagePostProcessController(
    private val blurFacesInAccessibilityImagesUseCase: BlurFacesInAccessibilityImagesUseCase,
    private val blurFacesInLatestAccessibilityImagesUseCase: BlurFacesInLatestBuildingAccessibilityImagesUseCase
) {
    @PostMapping("/blurFacesInPlaceAccessibilityImages")
    fun blurFacesInPlaceAccessibilityImages(
        @RequestBody params: BlurFacesInPlaceAccessibilityImages,
    ) {
        // TODO: UpdateChallengeRank 처럼 IP 체크
//        blurFacesInAccessibilityImagesUseCase.handleAsync(params.placeAccessibilityId)
    }

    @PostMapping("/blurFacesInBuildingAccessibilityImages")
    fun blurFacesInBuildingAccessibilityImages(
        @RequestBody params: BlurFacesInBuildingAccessibilityImages,
    ) {
        // TODO: UpdateChallengeRank 처럼 IP 체크
//        blurFacesInAccessibilityImagesUseCase.handleAsync(params.buildingAccessibilityId)
    }

    @PostMapping("/blurFacesInLatestPlaceAccessibilityImages")
    fun blurFacesInLatestPlaceAccessibilityImages() {
        // TODO: UpdateChallengeRank 처럼 IP 체크
        blurFacesInLatestAccessibilityImagesUseCase.handleAsync()
    }

    @PostMapping("/blurFacesInLatestBuildingAccessibilityImages")
    fun blurFacesInLatestBuildingAccessibilityImages() {
        // TODO: UpdateChallengeRank 처럼 IP 체크
        blurFacesInLatestAccessibilityImagesUseCase.handleAsync()
    }
}

data class BlurFacesInPlaceAccessibilityImages(
    @field:JsonProperty("placeAccessibilityId")
    val placeAccessibilityId: String
)

data class BlurFacesInBuildingAccessibilityImages(
    @field:JsonProperty("placeAccessibilityId")
    val buildingAccessibilityId: String
)

