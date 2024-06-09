package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.BlurFacesInAccessibilityImagesUseCase
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityImagePostProcessController(
    private val blurFacesInAccessibilityImagesUseCase: BlurFacesInAccessibilityImagesUseCase,
) {
    @PostMapping("/blurFacesInAccessibilityImages")
    fun blurFacesInAccessibilityImages(
        @RequestBody request: BlurFacesInAccessibilityImages,
    ) {
        blurFacesInAccessibilityImagesUseCase.handleAsync(request.placeAccessibilityId)
    }
}

data class BlurFacesInAccessibilityImages(
    @field:JsonProperty("placeAccessibilityId")
    val placeAccessibilityId: String
)
