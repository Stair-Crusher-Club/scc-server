package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class AccessibilityImageMigrationService(
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val blurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository,
    private val accessibilityImageRepository: AccessibilityImageRepository,
    private val transactionManager: TransactionManager,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
) {

    fun migratePlaceAccessibility(placeAccessibilityId: String) {
        transactionManager.doInTransaction {
            val alreadyExists = accessibilityImageRepository.findByAccessibilityIdAndAccessibilityType(
                placeAccessibilityId,
                AccessibilityImage.AccessibilityType.Place
            )
            if (alreadyExists.isNotEmpty()) {
                return@doInTransaction
            }
            val placeAccessibility =
                placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId) ?: return@doInTransaction
            val blurHistories = blurringHistoryRepository.findByPlaceAccessibilityId(placeAccessibilityId).firstOrNull()
            val modifiedAccessibilityImages = placeAccessibility.imageUrls.map { oldImageUrl ->
                val matchingHistory = blurHistories?.let {
                    it.blurredImageUrls.zip(it.originalImageUrls)
                }?.find {
                    it.first == oldImageUrl // Blur 된 이미지라면 BlurURL 이 image 에 들어가있다.
                }
                val matchingOldImage = placeAccessibility.images.find { it.imageUrl == oldImageUrl }
                AccessibilityImage(
                    accessibilityId = placeAccessibility.id,
                    accessibilityType = AccessibilityImage.AccessibilityType.Place,
                    originalImageUrl = matchingHistory?.second ?: oldImageUrl,
                    blurredImageUrl = matchingHistory?.first,
                    thumbnailUrl = matchingOldImage?.thumbnailUrl,
                )
            }
            accessibilityImageRepository.saveAll(modifiedAccessibilityImages)
        }
    }

    fun migrateBuildingAccessibility(buildingAccessibilityId: String) {
        transactionManager.doInTransaction {
            val alreadyExists =
                accessibilityImageRepository.findByAccessibilityIdAndAccessibilityType(
                    buildingAccessibilityId,
                    AccessibilityImage.AccessibilityType.Building
                )
            if (alreadyExists.isNotEmpty()) {
                return@doInTransaction
            }
            val buildingAccessibility =
                buildingAccessibilityRepository.findByIdOrNull(buildingAccessibilityId) ?: return@doInTransaction
            val blurHistories =
                blurringHistoryRepository.findByBuildingAccessibilityId(buildingAccessibilityId).firstOrNull()
            val modifiedElevatorAccessibilityImages = buildingAccessibility.elevatorImageUrls.map { oldImageUrl ->
                val matchingHistory = blurHistories?.let {
                    it.blurredImageUrls.zip(it.originalImageUrls)
                }?.find {
                    it.first == oldImageUrl // Blur 된 이미지라면 BlurURL 이 image 에 들어가있다.
                }
                val matchingOldImage = buildingAccessibility.elevatorImages.find { it.imageUrl == oldImageUrl }
                AccessibilityImage(
                    accessibilityId = buildingAccessibility.id,
                    accessibilityType = AccessibilityImage.AccessibilityType.Building,
                    imageType = AccessibilityImage.ImageType.Elevator,
                    originalImageUrl = matchingHistory?.second ?: oldImageUrl,
                    blurredImageUrl = matchingHistory?.first,
                    thumbnailUrl = matchingOldImage?.thumbnailUrl,
                )
            }
            val modifiedEntranceAccessibilityImages = buildingAccessibility.entranceImageUrls.map { oldImageUrl ->
                val matchingHistory = blurHistories?.let {
                    it.blurredImageUrls.zip(it.originalImageUrls)
                }?.find {
                    it.first == oldImageUrl // Blur 된 이미지라면 BlurURL 이 image 에 들어가있다.
                }
                val matchingOldImage = buildingAccessibility.entranceImages.find { it.imageUrl == oldImageUrl }
                AccessibilityImage(
                    accessibilityId = buildingAccessibility.id,
                    accessibilityType = AccessibilityImage.AccessibilityType.Building,
                    imageType = AccessibilityImage.ImageType.Entrance,
                    originalImageUrl = matchingHistory?.second ?: oldImageUrl,
                    blurredImageUrl = matchingHistory?.first,
                    thumbnailUrl = matchingOldImage?.thumbnailUrl,
                )
            }
            accessibilityImageRepository.saveAll(modifiedElevatorAccessibilityImages)
            accessibilityImageRepository.saveAll(modifiedEntranceAccessibilityImages)
        }
    }
}
