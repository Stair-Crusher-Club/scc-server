package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.persistence.TransactionManager
import jakarta.persistence.EntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class AccessibilityImageMigrationService(
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val blurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository,
    private val accessibilityImageRepository: AccessibilityImageRepository,
    private val transactionManager: TransactionManager,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val entityManager: EntityManager,
) {

    fun migratePlaceAccessibility(placeAccessibilityId: String) {
        transactionManager.doInTransaction {
            val alreadyExists = accessibilityImageRepository.findByAccessibilityIdAndAccessibilityType(
                placeAccessibilityId,
                AccessibilityImage.AccessibilityType.Place
            )
            val hasDisplayOrder = alreadyExists.any { it.displayOrder != null }
            if (alreadyExists.isNotEmpty() && hasDisplayOrder) {
                entityManager.flush()
                entityManager.clear()
                return@doInTransaction
            }
            val placeAccessibility = placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId)
            if (placeAccessibility == null) {
                entityManager.flush()
                entityManager.clear()
                return@doInTransaction
            }
            val blurHistories = blurringHistoryRepository.findByPlaceAccessibilityId(placeAccessibilityId).firstOrNull()
            val modifiedAccessibilityImages = placeAccessibility.oldImageUrls.mapIndexed { index, oldImageUrl ->
                val matchingHistory = blurHistories?.let {
                    it.blurredImageUrls.zip(it.originalImageUrls)
                }?.find {
                    it.first == oldImageUrl // Blur 된 이미지라면 BlurURL 이 image 에 들어가있다.
                }
                val matchingOldImage = placeAccessibility.oldImages.find { it.imageUrl == oldImageUrl }

                // 썸네일이 블러되지 않은 값으로 들어가 있을 수도 있지만, 우선은 이렇게만 처리해둔다.
                val isAlreadyPostProcessed = blurHistories != null && matchingOldImage?.thumbnailUrl != null

                val alreadyExistingImage = alreadyExists.find { it.originalImageUrl == (matchingHistory?.second ?: oldImageUrl) }
                if (alreadyExistingImage != null) {
                    alreadyExistingImage.displayOrder = index
                    alreadyExistingImage
                } else {
                    AccessibilityImage(
                        accessibilityId = placeAccessibility.id,
                        accessibilityType = AccessibilityImage.AccessibilityType.Place,
                        originalImageUrl = matchingHistory?.second ?: oldImageUrl,
                        blurredImageUrl = matchingHistory?.first,
                        thumbnailUrl = matchingOldImage?.thumbnailUrl,
                        lastPostProcessedAt = if (isAlreadyPostProcessed) SccClock.instant() else null,
                        displayOrder = index,
                    )
                }
            }
            accessibilityImageRepository.saveAll(modifiedAccessibilityImages.filterNotNull())
            entityManager.flush()
            entityManager.clear()
        }
    }

    fun migrateBuildingAccessibility(buildingAccessibilityId: String) {
        transactionManager.doInTransaction {
            val alreadyExists =
                accessibilityImageRepository.findByAccessibilityIdAndAccessibilityType(
                    buildingAccessibilityId,
                    AccessibilityImage.AccessibilityType.Building
                )
            val hasDisplayOrder = alreadyExists.any { it.displayOrder != null }
            if (alreadyExists.isNotEmpty() && hasDisplayOrder) {
                entityManager.flush()
                entityManager.clear()
                return@doInTransaction
            }
            val buildingAccessibility = buildingAccessibilityRepository.findByIdOrNull(buildingAccessibilityId)
            if (buildingAccessibility == null) {
                entityManager.flush()
                entityManager.clear()
                return@doInTransaction
            }
            val blurHistories =
                blurringHistoryRepository.findByBuildingAccessibilityId(buildingAccessibilityId).firstOrNull()
            val modifiedElevatorAccessibilityImages = buildingAccessibility.oldElevatorImageUrls.mapIndexed { index, oldImageUrl ->
                val matchingHistory = blurHistories?.let {
                    it.blurredImageUrls.zip(it.originalImageUrls)
                }?.find {
                    it.first == oldImageUrl // Blur 된 이미지라면 BlurURL 이 image 에 들어가있다.
                }
                val matchingOldImage = buildingAccessibility.oldElevatorImages.find { it.imageUrl == oldImageUrl }

                // 썸네일이 블러되지 않은 값으로 들어가 있을 수도 있지만, 우선은 이렇게만 처리해둔다.
                val isAlreadyPostProcessed = blurHistories != null && matchingOldImage?.thumbnailUrl != null

                val alreadyExistingImage = alreadyExists.find { it.originalImageUrl == (matchingHistory?.second ?: oldImageUrl) }
                if (alreadyExistingImage != null) {
                    alreadyExistingImage.displayOrder = index
                    alreadyExistingImage
                } else {
                    AccessibilityImage(
                        accessibilityId = buildingAccessibility.id,
                        accessibilityType = AccessibilityImage.AccessibilityType.Building,
                        imageType = AccessibilityImage.ImageType.Elevator,
                        originalImageUrl = matchingHistory?.second ?: oldImageUrl,
                        blurredImageUrl = matchingHistory?.first,
                        thumbnailUrl = matchingOldImage?.thumbnailUrl,
                        lastPostProcessedAt = if (isAlreadyPostProcessed) SccClock.instant() else null,
                        displayOrder = index,
                    )
                }
            }
            val modifiedEntranceAccessibilityImages = buildingAccessibility.oldEntranceImageUrls.mapIndexed { index, oldImageUrl ->
                val matchingHistory = blurHistories?.let {
                    it.blurredImageUrls.zip(it.originalImageUrls)
                }?.find {
                    it.first == oldImageUrl // Blur 된 이미지라면 BlurURL 이 image 에 들어가있다.
                }
                val matchingOldImage = buildingAccessibility.oldEntranceImages.find { it.imageUrl == oldImageUrl }

                // 썸네일이 블러되지 않은 값으로 들어가 있을 수도 있지만, 우선은 이렇게만 처리해둔다.
                val isAlreadyPostProcessed = blurHistories != null && matchingOldImage?.thumbnailUrl != null

                val alreadyExistingImage = alreadyExists.find { it.originalImageUrl == (matchingHistory?.second ?: oldImageUrl) }
                if (alreadyExistingImage != null) {
                    alreadyExistingImage.displayOrder = index
                    alreadyExistingImage
                } else {
                    AccessibilityImage(
                        accessibilityId = buildingAccessibility.id,
                        accessibilityType = AccessibilityImage.AccessibilityType.Building,
                        imageType = AccessibilityImage.ImageType.Entrance,
                        originalImageUrl = matchingHistory?.second ?: oldImageUrl,
                        blurredImageUrl = matchingHistory?.first,
                        thumbnailUrl = matchingOldImage?.thumbnailUrl,
                        lastPostProcessedAt = if (isAlreadyPostProcessed) SccClock.instant() else null,
                        displayOrder = index,
                    )
                }
            }
            accessibilityImageRepository.saveAll(modifiedElevatorAccessibilityImages.filterNotNull())
            accessibilityImageRepository.saveAll(modifiedEntranceAccessibilityImages.filterNotNull())
            entityManager.flush()
            entityManager.clear()
        }
    }
}
