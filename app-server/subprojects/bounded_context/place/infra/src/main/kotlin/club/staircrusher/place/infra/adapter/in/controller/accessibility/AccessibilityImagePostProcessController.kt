package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.BlurFacesInLatestBuildingAccessibilityImagesUseCase
import club.staircrusher.place.application.port.`in`.accessibility.BlurFacesInLatestPlaceAccessibilityImagesUseCase
import club.staircrusher.spring_web.security.InternalIpAddressChecker
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityImagePostProcessController(
    private val blurFacesInLatestPlaceAccessibilityImagesUseCase: BlurFacesInLatestPlaceAccessibilityImagesUseCase,
    private val blurFacesInLatestBuildingAccessibilityImagesUseCase: BlurFacesInLatestBuildingAccessibilityImagesUseCase
) {
    @PostMapping("/blurFacesInLatestPlaceAccessibilityImages")
    fun blurFacesInLatestPlaceAccessibilityImages(request: HttpServletRequest) {
        InternalIpAddressChecker.check(request)
        blurFacesInLatestPlaceAccessibilityImagesUseCase.handleAsync()
    }

    @PostMapping("/blurFacesInLatestBuildingAccessibilityImages")
    fun blurFacesInLatestBuildingAccessibilityImages(request: HttpServletRequest) {
        InternalIpAddressChecker.check(request)
        blurFacesInLatestBuildingAccessibilityImagesUseCase.handleAsync()
    }
}
