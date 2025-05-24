package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.ImageRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.domain.model.accessibility.Image
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Component
class AccessibilityImageMigrator(
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val blurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository,
    private val imageRepository: ImageRepository,
    private val transactionManager: TransactionManager,
) {

    fun migratePlaceAccessibility(placeAccessibilityId: String) {
        transactionManager.doInTransaction {
            val placeAccessibility =
                placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId) ?: return@doInTransaction
            val blurHistories = blurringHistoryRepository.findByPlaceAccessibilityId(placeAccessibilityId).firstOrNull()
            val modifiedImages = placeAccessibility.imageUrls.map { oldImageUrl ->
                val matchingHistory = blurHistories?.let {
                    it.blurredImageUrls.zip(it.originalImageUrls)
                }?.find {
                    it.first == oldImageUrl // Blur 된 이미지라면 BlurURL 이 image 에 들어가있다.
                }
                val matchingOldImage = placeAccessibility.images.find { it.imageUrl == oldImageUrl }
                Image(
                    accessibilityId = placeAccessibility.id,
                    accessibilityType = "Place",
                    imageUrl = matchingHistory?.second ?: oldImageUrl,
                    blurredImageUrl = matchingHistory?.first,
                    thumbnailUrl = matchingOldImage?.thumbnailUrl,
                )
            }
            imageRepository.saveAll(modifiedImages)
        }
    }

    fun migrateBuildingAccessibility(buildingAccessibilityId: String) {

    }
}
