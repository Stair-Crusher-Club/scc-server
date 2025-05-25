package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import org.hibernate.query.spi.Limit
import java.time.Instant

@Component
class AccessibilityImagePipeline(
    private val accessibilityImageFaceBlurringService: AccessibilityImageFaceBlurringService,
    private val accessibilityImageThumbnailService: AccessibilityImageThumbnailService,
    private val accessibilityImageRepository: AccessibilityImageRepository,
    private val transactionManager: TransactionManager,
) {
    suspend fun postProcessImages(images: List<AccessibilityImage>) {
        val processedImages = images
            .let { accessibilityImageFaceBlurringService.blurImages(it) }
            .let { accessibilityImageThumbnailService.generateThumbnails(it) }
        transactionManager.doInTransaction {
            val now = Instant.now()
            processedImages.forEach { image ->
                image.lastPostProcessedTime = now
            }
            accessibilityImageRepository.saveAll(processedImages)
        }
    }
}
