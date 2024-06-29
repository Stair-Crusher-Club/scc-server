package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImagesBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import java.util.concurrent.Executors

@Component
class BlurFacesInLatestPlaceAccessibilityImagesUseCase(
    private val blurFacesInAccessibilityImagesUseCase: BlurFacesInAccessibilityImagesUseCase,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val accessibilityImagesBlurringHistoryRepository: AccessibilityImagesBlurringHistoryRepository,
) {
    private val taskExecutor = Executors.newCachedThreadPool()

    fun handleAsync() {
        taskExecutor.execute {
            handle()
        }
    }

    fun handle() {
        val latestHistory = accessibilityImagesBlurringHistoryRepository.findLatestPlaceHistoryOrNull()
        val lastBlurredPlaceAccessibility = latestHistory?.let { history ->
            history.placeAccessibilityId?.let { placeAccessibilityRepository.findByIdOrNull(it) }
        }
        val targetAccessibility =
            placeAccessibilityRepository.findByCreatedAtGreaterThanAndOrderByCreatedAtAsc(createdAt = lastBlurredPlaceAccessibility?.createdAt)
                .firstOrNull() ?: return
        blurFacesInAccessibilityImagesUseCase.handle(targetAccessibility.id)
    }
}
