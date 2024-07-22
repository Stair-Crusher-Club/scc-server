package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import kotlinx.coroutines.runBlocking

@Component
class BlurFacesInPlaceAccessibilityImagesUseCase(
    private val accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService
) {
    fun handle(placeAccessibilityId: String) = runBlocking {
        accessibilityImageFaceBlurringService.blurFacesInPlaceAccessibility(placeAccessibilityId)
    }
}
