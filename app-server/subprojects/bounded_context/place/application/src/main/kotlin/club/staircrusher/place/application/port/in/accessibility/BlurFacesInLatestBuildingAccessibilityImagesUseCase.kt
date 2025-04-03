package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.AccessibilityImageFaceBlurringHistory
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.runBlocking
import java.time.Instant
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
        val targetAccessibility = transactionManager.doInTransaction {
            // 가장 최근에 블러 처리한 accessibility 보다 이후에 생성된 accessibility 를 찾는다
            val recentHistories = accessibilityImageFaceBlurringHistoryRepository.findTop5ByBuildingAccessibilityIdIsNotNullOrderByCreatedAtDesc()
            val recentlyBlurredBuildingAccessibilities = buildingAccessibilityRepository.findByIdIn(recentHistories.mapNotNull { it.buildingAccessibilityId })
            val mostRecentCreatedAt = recentlyBlurredBuildingAccessibilities.maxByOrNull { it.createdAt }?.createdAt

            val blurCandidateBuildingAccessibilities = buildingAccessibilityRepository.findTop5ByCreatedAtAfterAndDeletedAtIsNullOrderByCreatedAtAscIdDesc(
                createdAt = mostRecentCreatedAt ?: Instant.EPOCH
            )

            val alreadyBlurredBuildingAccessibilityIds = accessibilityImageFaceBlurringHistoryRepository.findByBuildingAccessibilityIdIn(blurCandidateBuildingAccessibilities.map { it.id })
                .groupBy { it.buildingAccessibilityId!! }
                .filter { it.value.isNotEmpty() }
                .map { it.key }

            blurCandidateBuildingAccessibilities.firstOrNull { it.id !in alreadyBlurredBuildingAccessibilityIds }
        } ?: return

        val result = runBlocking { accessibilityImageFaceBlurringService.blurFacesInBuildingAccessibility(targetAccessibility.id) } ?: return
        transactionManager.doInTransaction {
            val entranceResults = result.entranceResults
            val elevatorResults = result.elevatorResults

            val entranceImageUrls = entranceResults.map { it.blurredImageUrl }
            targetAccessibility.updateEntranceImages(entranceImageUrls.map { AccessibilityImage(imageUrl = it, thumbnailUrl = null) })

            val elevatorImageUrls = elevatorResults.map { it.blurredImageUrl }
            targetAccessibility.updateElevatorImages(elevatorImageUrls.map { AccessibilityImage(imageUrl = it, thumbnailUrl = null) })

            buildingAccessibilityRepository.save(targetAccessibility)

            val originalImageUrls = (entranceResults + elevatorResults).map { it.originalImageUrl }
            val blurredImageUrls = (entranceResults + elevatorResults).filter { it.isBlurred() }.map { it.blurredImageUrl }
            val detectedPeopleCounts = (entranceResults + elevatorResults).map { it.detectedPeopleCount }
            accessibilityImageFaceBlurringHistoryRepository.save(
                AccessibilityImageFaceBlurringHistory(
                    id = EntityIdGenerator.generateRandom(),
                    placeAccessibilityId = null, buildingAccessibilityId = targetAccessibility.id,
                    originalImageUrls = originalImageUrls,
                    blurredImageUrls = blurredImageUrls,
                    detectedPeopleCounts = detectedPeopleCounts,
                )
            )
        }
    }
}
