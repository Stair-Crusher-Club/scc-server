package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import kotlinx.coroutines.runBlocking

@Component
class BlurFacesInBuildingAccessibilityImagesUseCase(
    private val accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService
) {

    fun handle(buildingAccessibilityId: String) = runBlocking {
        accessibilityImageFaceBlurringService.blurFacesInBuildingAccessibility(buildingAccessibilityId)
    }
}
