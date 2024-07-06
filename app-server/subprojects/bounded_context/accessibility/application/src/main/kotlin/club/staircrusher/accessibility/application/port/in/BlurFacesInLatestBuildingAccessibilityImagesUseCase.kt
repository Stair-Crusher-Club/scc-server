package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

@Component
class BlurFacesInLatestBuildingAccessibilityImagesUseCase(
    private val accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository,
    private val transactionManager: TransactionManager,
) {
    private val taskExecutor = Executors.newCachedThreadPool()

    fun handleAsync() {
        taskExecutor.execute {
            handle()
        }
    }

    fun handle() {
        val targetAccessibility: BuildingAccessibility = transactionManager.doInTransaction {
            val latestHistory = accessibilityImageFaceBlurringHistoryRepository.findLatestBuildingHistoryOrNull()
            val lastBlurredBuildingAccessibility = latestHistory?.let { history ->
                history.buildingAccessibilityId?.let { buildingAccessibilityRepository.findByIdOrNull(it) }
            }
            buildingAccessibilityRepository.findByCreatedAtGreaterThanAndOrderByCreatedAtAsc(createdAt = lastBlurredBuildingAccessibility?.createdAt)
                .firstOrNull()
        } ?: return
        runBlocking {
            accessibilityImageFaceBlurringService.blurFacesInBuildingAccessibility(targetAccessibility.id)
        }
    }
}
