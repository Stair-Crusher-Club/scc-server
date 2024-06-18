package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.BlurFacesInAccessibilityImagesUseCase
import club.staircrusher.accessibility.application.port.`in`.BlurFacesInLatestAccessibilityImagesUseCase
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityImagePostProcessController(
    private val blurFacesInAccessibilityImagesUseCase: BlurFacesInAccessibilityImagesUseCase,
    private val blurFacesInLatestAccessibilityImagesUseCase: BlurFacesInLatestAccessibilityImagesUseCase
) {
    @PostMapping("/blurFacesInAccessibilityImages")
    fun blurFacesInAccessibilityImages(
        @RequestBody params: BlurFacesInAccessibilityImages,
    ) {
        // TODO: UpdateChallengeRank 처럼 IP 체크
        blurFacesInAccessibilityImagesUseCase.handleAsync(params.placeAccessibilityId)
    }

    @PostMapping("/blurFacesInLatestPlaceAccessibilityImages")
    fun blurFacesInLatestPlaceAccessibilityImages() {
        // TODO: UpdateChallengeRank 처럼 IP 체크
        blurFacesInLatestAccessibilityImagesUseCase.handleAsync()
    }
}

data class BlurFacesInAccessibilityImages(
    @field:JsonProperty("placeAccessibilityId")
    val placeAccessibilityId: String
)
