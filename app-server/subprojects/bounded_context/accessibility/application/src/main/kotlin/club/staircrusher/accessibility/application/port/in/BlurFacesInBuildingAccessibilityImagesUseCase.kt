package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors


@Component
class BlurFacesInBuildingAccessibilityImagesUseCase(
    private val accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService
) {
    private val taskExecutor = Executors.newCachedThreadPool()

    fun handleAsync(buildingAccessibilityId: String) {
        taskExecutor.execute {
            runBlocking {
                accessibilityImageFaceBlurringService.blurFacesInBuildingAccessibility(buildingAccessibilityId)
            }
        }
    }
}
