package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.BlurFacesInLatestBuildingAccessibilityImagesUseCase
import club.staircrusher.accessibility.application.port.`in`.BlurFacesInLatestPlaceAccessibilityImagesUseCase
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityImagePostProcessController(
    private val blurFacesInLatestPlaceAccessibilityImagesUseCase: BlurFacesInLatestPlaceAccessibilityImagesUseCase,
    private val blurFacesInLatestBuildingAccessibilityImagesUseCase: BlurFacesInLatestBuildingAccessibilityImagesUseCase
) {
    @PostMapping("/blurFacesInLatestPlaceAccessibilityImages")
    fun blurFacesInLatestPlaceAccessibilityImages() {
        // TODO: UpdateChallengeRank 처럼 IP 체크
        blurFacesInLatestPlaceAccessibilityImagesUseCase.handleAsync()
    }

    @PostMapping("/blurFacesInLatestBuildingAccessibilityImages")
    fun blurFacesInLatestBuildingAccessibilityImages() {
        // TODO: UpdateChallengeRank 처럼 IP 체크
        blurFacesInLatestBuildingAccessibilityImagesUseCase.handleAsync()
    }
}
