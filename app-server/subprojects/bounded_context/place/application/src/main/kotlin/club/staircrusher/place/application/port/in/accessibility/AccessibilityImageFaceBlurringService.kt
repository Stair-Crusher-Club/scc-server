package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.image.application.port.out.file_management.FileManagementService
import club.staircrusher.place.application.port.`in`.accessibility.image.ImageProcessor
import club.staircrusher.place.application.port.out.accessibility.DetectFacesService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging

@Component
class AccessibilityImageFaceBlurringService(
    private val accessibilityImageService: AccessibilityImageService,
    private val imageProcessor: ImageProcessor,
    private val detectFacesService: DetectFacesService,
    private val fileManagementService: FileManagementService,
    private val transactionManager: TransactionManager,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun blurFacesInPlaceAccessibility(placeAccessibilityId: String): PlaceAccessibilityBlurResult? {
        val placeAccessibility = transactionManager.doInTransaction {
            accessibilityImageService.doMigratePlaceAccessibilityImageUrlsToImagesIfNeeded(placeAccessibilityId = placeAccessibilityId)
        }
        if (placeAccessibility == null) {
            logger.error("PlaceAccessibility(${placeAccessibilityId}) not found.")
            return null
        }
        val imageUrls = placeAccessibility.images.map { it.imageUrl }
        val result = detectAndBlurFaces(imageUrls)
        return PlaceAccessibilityBlurResult(result)
    }

    suspend fun blurFacesInBuildingAccessibility(buildingAccessibilityId: String): BuildingAccessibilityBlurResult? {
        val buildingAccessibility = transactionManager.doInTransaction {
            accessibilityImageService.doMigrateBuildingAccessibilityImageUrlsToImagesIfNeeded(buildingAccessibilityId = buildingAccessibilityId)
        }
        if (buildingAccessibility == null) return null
        val entranceResult = detectAndBlurFaces(buildingAccessibility.entranceImages.map { it.imageUrl })
        val elevatorResult = detectAndBlurFaces(buildingAccessibility.elevatorImages.map { it.imageUrl })
        return BuildingAccessibilityBlurResult(entranceResult, elevatorResult)
    }

    private suspend fun detectAndBlurFaces(imageUrls: List<String>): List<BlurResult> = coroutineScope {
        imageUrls.map { async { detectAndBlurFaces(it) } }.awaitAll()
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
            val detected = detectFacesService.detect(imageUrl)
            val imageBytes = detected.imageBytes
            if (detected.positions.isEmpty()) return BlurResult(
                originalImageUrl = imageUrl,
                blurredImageUrl = imageUrl,
                detectedPeopleCount = 0
            )
            val (blurredImageUrl, detectedPositions) = run {
                val outputByteArray = imageProcessor.blur(imageBytes, extension, detected.positions)
                val blurredImageUrl = fileManagementService.uploadAccessibilityImage("${name}_b.$extension", outputByteArray)
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
                blurredImageUrl = imageUrl,
                detectedPeopleCount = 0
            )
        }
    }

    data class BlurResult(
        val originalImageUrl: String,
        val blurredImageUrl: String,
        val detectedPeopleCount: Int,
    ) {
        fun isBlurred() = originalImageUrl != blurredImageUrl
    }

    data class PlaceAccessibilityBlurResult(
        val entranceResults: List<BlurResult>,
    )

    data class BuildingAccessibilityBlurResult(
        val entranceResults: List<BlurResult>,
        val elevatorResults: List<BlurResult>,
    )
}
