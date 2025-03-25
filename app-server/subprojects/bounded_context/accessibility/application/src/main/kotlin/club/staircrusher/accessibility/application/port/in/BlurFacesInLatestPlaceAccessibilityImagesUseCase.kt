package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.env.SccEnv
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
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
        if (SccEnv.isProduction().not()) {
            return
        }
        taskExecutor.execute {
            handle()
        }
    }

    fun handle() {
        val targetAccessibility = transactionManager.doInTransaction(isReadOnly = true) {
            // 가장 최근에 블러 처리한 accessibility 보다 이후에 생성된 accessibility 를 찾는다
            // place accessibility 가 hard delete 되는 경우를 대비해 여러개를 가져와서 비교한다
            val recentHistories = accessibilityImageFaceBlurringHistoryRepository.findTop5ByPlaceAccessibilityIdIsNotNullOrderByCreatedAtDesc()
            val recentlyBlurredPlaceAccessibilities = placeAccessibilityRepository.findByIdIn(recentHistories.mapNotNull { it.placeAccessibilityId })
            val mostRecentCreatedAt = recentlyBlurredPlaceAccessibilities.maxByOrNull { it.createdAt }?.createdAt

            val blurCandidatePlaceAccessibilities = placeAccessibilityRepository.findTop5ByCreatedAtAfterAndDeletedAtIsNullOrderByCreatedAtAscIdDesc(
                createdAt = mostRecentCreatedAt ?: Instant.EPOCH
            )

            val alreadyBlurredPlaceAccessibilityIds = accessibilityImageFaceBlurringHistoryRepository.findByPlaceAccessibilityIdIn(blurCandidatePlaceAccessibilities.map { it.id })
                .groupBy { it.placeAccessibilityId!! }
                .filter { it.value.isNotEmpty() }
                .map { it.key }

            blurCandidatePlaceAccessibilities.firstOrNull { it.id !in alreadyBlurredPlaceAccessibilityIds }
        } ?: return

        val result = runBlocking { accessibilityImageFaceBlurringService.blurFacesInPlaceAccessibility(targetAccessibility.id) } ?: return
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
                )
            )
        }
    }
}
