package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
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
            val latestHistory = accessibilityImageFaceBlurringHistoryRepository.findFirstByPlaceAccessibilityIdIsNotNullOrderByCreatedAtDesc()
            val lastBlurredPlaceAccessibility = latestHistory?.let { history ->
                history.placeAccessibilityId?.let { placeAccessibilityRepository.findByIdOrNull(it) }
            }
            placeAccessibilityRepository.findBlurringTargetAccessibility(
                createdAt = lastBlurredPlaceAccessibility?.createdAt ?: Instant.EPOCH
            )
        } ?: return
        val result =
            runBlocking { accessibilityImageFaceBlurringService.blurFacesInPlaceAccessibility(targetAccessibility.id) }
                ?: return
        val entranceResults = result.entranceResults
        transactionManager.doInTransaction {
            val imageUrls = entranceResults.map { it.blurredImageUrl }
            targetAccessibility.updateImages(imageUrls.map { AccessibilityImage(imageUrl = it, thumbnailUrl = null) })
            placeAccessibilityRepository.save(targetAccessibility)
            accessibilityImageFaceBlurringHistoryRepository.save(
                AccessibilityImageFaceBlurringHistory(
                    id = EntityIdGenerator.generateRandom(),
                    placeAccessibilityId = targetAccessibility.id,
                    buildingAccessibilityId = null,
                    originalImageUrls = entranceResults.map { it.originalImageUrl },
                    blurredImageUrls = entranceResults.filter { it.isBlurred() }.map { it.blurredImageUrl },
                    detectedPeopleCounts = entranceResults.map { it.detectedPeopleCount },
                    createdAt = SccClock.instant(),
                    updatedAt = SccClock.instant()
                )
            )
        }
    }
}
