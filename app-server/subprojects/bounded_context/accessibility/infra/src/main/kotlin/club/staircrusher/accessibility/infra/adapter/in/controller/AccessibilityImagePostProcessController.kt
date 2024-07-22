package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.BlurFacesInBuildingAccessibilityImagesUseCase
import club.staircrusher.accessibility.application.port.`in`.BlurFacesInLatestBuildingAccessibilityImagesUseCase
import club.staircrusher.accessibility.application.port.`in`.BlurFacesInLatestPlaceAccessibilityImagesUseCase
import club.staircrusher.accessibility.application.port.`in`.BlurFacesInPlaceAccessibilityImagesUseCase
import club.staircrusher.accessibility.infra.adapter.`in`.model.BlurFacesInBuildingAccessibilityImagesParams
import club.staircrusher.accessibility.infra.adapter.`in`.model.BlurFacesInPlaceAccessibilityImagesParams
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityImagePostProcessController(
    private val blurFacesInPlaceAccessibilityImagesUseCase: BlurFacesInPlaceAccessibilityImagesUseCase,
    private val blurFacesInBuildingAccessibilityImagesUseCase: BlurFacesInBuildingAccessibilityImagesUseCase,
    private val blurFacesInLatestPlaceAccessibilityImagesUseCase: BlurFacesInLatestPlaceAccessibilityImagesUseCase,
    private val blurFacesInLatestBuildingAccessibilityImagesUseCase: BlurFacesInLatestBuildingAccessibilityImagesUseCase
) {
    @PostMapping("/blurFacesInPlaceAccessibilityImages")
    fun blurFacesInPlaceAccessibilityImages(
        @RequestBody params: BlurFacesInPlaceAccessibilityImagesParams,
    ) {
        // TODO: UpdateChallengeRank 처럼 IP 체크
        blurFacesInPlaceAccessibilityImagesUseCase.handle(params.placeAccessibilityId)
    }

    @PostMapping("/blurFacesInBuildingAccessibilityImages")
    fun blurFacesInBuildingAccessibilityImages(
        @RequestBody params: BlurFacesInBuildingAccessibilityImagesParams,
    ) {
        // TODO: UpdateChallengeRank 처럼 IP 체크
        blurFacesInBuildingAccessibilityImagesUseCase.handle(params.buildingAccessibilityId)
    }

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
