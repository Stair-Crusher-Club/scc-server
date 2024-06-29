package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImagesBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import java.util.concurrent.Executors

@Component
class BlurFacesInLatestBuildingAccessibilityImagesUseCase(
    private val accessibilityImageFaceBlurService: AccessibilityImageFaceBlurService,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val accessibilityImagesBlurringHistoryRepository: AccessibilityImagesBlurringHistoryRepository,
) {
    private val taskExecutor = Executors.newCachedThreadPool()

    fun handleAsync() {
        taskExecutor.execute {
            handle()
        }
    }

    fun handle() {
        val latestHistory = accessibilityImagesBlurringHistoryRepository.findLatestBuildingHistoryOrNull()
        val lastBlurredBuildingAccessibility = latestHistory?.let { history ->
            history.buildingAccessibilityId?.let { buildingAccessibilityRepository.findByIdOrNull(it) }
        }
        val targetAccessibility =
            buildingAccessibilityRepository.findByCreatedAtGreaterThanAndOrderByCreatedAtAsc(createdAt = lastBlurredBuildingAccessibility?.createdAt)
                .firstOrNull() ?: return
        accessibilityImageFaceBlurService.blurFacesInBuildingAccessibility(targetAccessibility.id)
    }
}
