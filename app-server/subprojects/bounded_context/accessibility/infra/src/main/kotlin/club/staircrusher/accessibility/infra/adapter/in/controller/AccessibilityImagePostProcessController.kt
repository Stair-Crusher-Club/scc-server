package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.BlurFacesInAccessibilityImagesUseCase
import club.staircrusher.accessibility.application.port.`in`.BlurFacesInLatestAccessibilityImagesUseCase
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.util.matcher.IpAddressMatcher
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
        request: HttpServletRequest,
        @RequestBody params: BlurFacesInAccessibilityImages,
    ) {
        // TODO: 잘 돌아가는지 확인 후 authority 확인
        // checkAuthority(request)
        blurFacesInAccessibilityImagesUseCase.handleAsync(params.placeAccessibilityId)
    }

    @PostMapping("/blurFacesInLatestPlaceAccessibilityImages")
    fun blurFacesInLatestPlaceAccessibilityImages(request: HttpServletRequest) {
        // TODO: 잘 돌아가는지 확인 후 authority 확인
        // checkAuthority(request)
        blurFacesInLatestAccessibilityImagesUseCase.handleAsync()
    }

    private fun checkAuthority(request: HttpServletRequest) {
        val clusterIpAddressMatcher = IpAddressMatcher("10.42.0.0/16")
        val localIpAddressMatcher = IpAddressMatcher("127.0.0.1/32")
        if (
            !clusterIpAddressMatcher.matches(request)
            && !localIpAddressMatcher.matches(request)
        ) {
            throw IllegalArgumentException("Unauthorized")
        }
    }
}

data class BlurFacesInAccessibilityImages(
    @field:JsonProperty("placeAccessibilityId")
    val placeAccessibilityId: String
)
