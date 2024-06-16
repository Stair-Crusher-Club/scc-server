package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImagesBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import java.util.concurrent.Executors

@Component
class BlurFacesInLatestAccessibilityImagesUseCase(
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
        val latestHistory = accessibilityImagesBlurringHistoryRepository.findLatestHistoryOrNull()
        val targetAccessibility = latestHistory?.let { history ->
            val placeAccessibility =
                history.placeAccessibilityId?.let { placeAccessibilityRepository.findByPlaceId(it) } ?: return@let null
            placeAccessibilityRepository.searchForAdmin(
                placeName = null,
                createdAtFrom = null,
                createdAtToExclusive = null,
                cursorCreatedAt = placeAccessibility.createdAt,
                cursorId = placeAccessibility.id,
                limit = 1,
            ).firstOrNull()
        } ?: placeAccessibilityRepository.findOldest() ?: return
        blurFacesInAccessibilityImagesUseCase.handle(targetAccessibility.id)
    }
}
