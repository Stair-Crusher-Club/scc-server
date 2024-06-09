package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

@Component
class BlurFacesInLatestPlaceAccessibilityImagesUseCase(
    private val accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService,
    private val accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val transactionManager: TransactionManager,
) {
    private val taskExecutor = Executors.newCachedThreadPool()

    fun handleAsync() {
        taskExecutor.execute {
            handle()
        }
    }

    fun handle() {
        val targetAccessibility: PlaceAccessibility = transactionManager.doInTransaction {
            val latestHistory = accessibilityImageFaceBlurringHistoryRepository.findLatestPlaceHistoryOrNull()
            val lastBlurredPlaceAccessibility = latestHistory?.let { history ->
                history.placeAccessibilityId?.let { placeAccessibilityRepository.findByIdOrNull(it) }
            }
            placeAccessibilityRepository.findByCreatedAtGreaterThanAndOrderByCreatedAtAsc(createdAt = lastBlurredPlaceAccessibility?.createdAt)
                .firstOrNull()
        } ?: return
        runBlocking {
            accessibilityImageFaceBlurringService.blurFacesInPlaceAccessibility(targetAccessibility.id)
        }
    }
}
