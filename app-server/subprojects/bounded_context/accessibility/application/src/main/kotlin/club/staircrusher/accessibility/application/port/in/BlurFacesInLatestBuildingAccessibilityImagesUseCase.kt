package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

@Component
class BlurFacesInLatestBuildingAccessibilityImagesUseCase(
    private val accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService,
    private val accessibilityImageService: AccessibilityImageService,
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
            buildingAccessibilityRepository.findOneOrNullByCreatedAtGreaterThanAndOrderByCreatedAtAsc(createdAt = lastBlurredBuildingAccessibility?.createdAt)
        } ?: return
        val result = runBlocking { accessibilityImageFaceBlurringService.blurFacesInBuildingAccessibility(targetAccessibility.id) } ?: return
        transactionManager.doInTransaction {
            val entranceResults = result.entranceResults
            val elevatorResults = result.elevatorResults
            accessibilityImageService.doUpdateBuildingAccessibilityOriginalImages(
                targetAccessibility.id, entranceResults.map { it.blurredImageUrl }, elevatorResults.map { it.blurredImageUrl }
            )
            val originalImageUrls = (entranceResults + elevatorResults).map { it.originalImageUrl }
            val blurredImageUrls = (entranceResults + elevatorResults).filter { it.isBlurred() }.map { it.blurredImageUrl }
            val detectedPeopleCounts = (entranceResults + elevatorResults).map { it.detectedPeopleCount }
            AccessibilityImageFaceBlurringHistory(
                id = EntityIdGenerator.generateRandom(),
                placeAccessibilityId = null, buildingAccessibilityId = targetAccessibility.id,
                originalImageUrls = originalImageUrls,
                blurredImageUrls = blurredImageUrls,
                detectedPeopleCounts = detectedPeopleCounts,
                createdAt = SccClock.instant(), updatedAt = SccClock.instant()
            )
        }
    }
}
