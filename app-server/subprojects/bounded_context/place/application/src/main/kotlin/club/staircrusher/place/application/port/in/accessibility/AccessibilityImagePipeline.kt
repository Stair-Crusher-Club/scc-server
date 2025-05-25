package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class AccessibilityImagePipeline(
    private val accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService,
    private val accessibilityImageThumbnailService: AccessibilityImageThumbnailService,
    private val accessibilityImageRepository: AccessibilityImageRepository,
    private val transactionManager: TransactionManager,
) {
    suspend fun postProcessPlaceAccessibility(placeAccessibility: PlaceAccessibility) {
        val processedImages = placeAccessibility.newAccessibilityImages
            .let { accessibilityImageFaceBlurringService.blurImages(it) }
            .let { accessibilityImageThumbnailService.generateThumbnails(it) }
        transactionManager.doInTransaction {
            accessibilityImageRepository.saveAll(processedImages)
        }
    }

    suspend fun postProcessBuildingAccessibility(buildingAccessibility: BuildingAccessibility) {
        val processedImages = (buildingAccessibility.newEntranceAccessibilityImages + buildingAccessibility.newElevatorAccessibilityImages)
            .let { accessibilityImageFaceBlurringService.blurImages(it) }
            .let { accessibilityImageThumbnailService.generateThumbnails(it) }
        transactionManager.doInTransaction {
            accessibilityImageRepository.saveAll(processedImages)
        }
    }
}
