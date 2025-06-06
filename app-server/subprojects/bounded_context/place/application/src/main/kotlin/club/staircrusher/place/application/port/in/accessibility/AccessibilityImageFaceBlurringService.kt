package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.image.application.port.out.file_management.FileManagementService
import club.staircrusher.place.application.port.`in`.accessibility.image.ImageProcessor
import club.staircrusher.place.application.port.out.accessibility.DetectFacesService
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.stdlib.di.annotation.Component
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import mu.KotlinLogging

@Component
class AccessibilityImageFaceBlurringService(
    private val imageProcessor: ImageProcessor,
    private val detectFacesService: DetectFacesService,
    private val fileManagementService: FileManagementService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun blurImage(accessibilityImage: AccessibilityImage): AccessibilityImage = coroutineScope {
        if (accessibilityImage.blurredImageUrl != null) return@coroutineScope accessibilityImage

        val blurResult = detectAndBlurFaces(accessibilityImage.originalImageUrl)
        accessibilityImage.blurredImageUrl = blurResult.blurredImageUrl

        return@coroutineScope accessibilityImage
    }

    @Suppress("ReturnCount")
    private suspend fun detectAndBlurFaces(imageUrl: String): BlurResult {
        try {
            val (name, extension) = imageUrl.split("/").last().split(".")
            if (listOf("jpg", "jpeg", "png", "webp").contains(extension).not()) {
                logger.info { "Detecting and blurring faces failed. $imageUrl is not image." }
                return BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = imageUrl,
                    detectedPeopleCount = 0
                )
            }
            val detected = withContext(Dispatchers.IO) { detectFacesService.detect(imageUrl) }
            val imageBytes = detected.imageBytes
            if (detected.positions.isEmpty()) return BlurResult(
                originalImageUrl = imageUrl,
                blurredImageUrl = imageUrl,
                detectedPeopleCount = 0
            )
            val (blurredImageUrl, detectedPositions) = run {
                val outputByteArray = withContext(Dispatchers.Default) {
                    imageProcessor.blur(imageBytes, extension, detected.positions)
                }
                val blurredImageUrl = withContext(Dispatchers.IO) {
                    fileManagementService.uploadAccessibilityImage("${name}_b.$extension", outputByteArray)
                }
                blurredImageUrl to detected.positions
            }
            return BlurResult(
                originalImageUrl = imageUrl,
                blurredImageUrl = blurredImageUrl ?: imageUrl,
                detectedPeopleCount = detectedPositions.size
            )
        } catch (e: Throwable) {
            logger.error(e) { "Detecting and blurring faces in the image($imageUrl) failed." }
            return BlurResult(
                originalImageUrl = imageUrl,
                blurredImageUrl = null,
                detectedPeopleCount = 0
            )
        }
    }

    data class BlurResult(
        val originalImageUrl: String,
        val blurredImageUrl: String?,
        val detectedPeopleCount: Int,
    ) {
        fun isBlurred() = originalImageUrl != blurredImageUrl
    }
}
