package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.image.ImageProcessor
import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
open class AccessibilityImageFaceBlurringService(
    private val accessibilityImageService: AccessibilityImageService,
    private val imageProcessor: ImageProcessor,
    private val detectFacesService: DetectFacesService,
    private val fileManagementService: FileManagementService,
    private val transactionManager: TransactionManager,
) {
    suspend fun blurFacesInPlaceAccessibility(placeAccessibilityId: String): PlaceAccessibilityBlurResult? {
        val placeAccessibility = transactionManager.doInTransaction {
            accessibilityImageService.doMigratePlaceAccessibilityImageUrlsToImagesIfNeeded(placeAccessibilityId = placeAccessibilityId)
        }
        if (placeAccessibility == null) return null
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

    private suspend fun detectAndBlurFaces(imageUrls: List<String>): List<BlurResult> {
        return imageUrls.map { imageUrl ->
            try {
                val detected = detectFacesService.detect(imageUrl)
                val imageBytes = detected.imageBytes
                if (detected.positions.isEmpty()) return@map BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = imageUrl,
                    detectedPeopleCount = 0
                )
                val (blurredImageUrl, detectedPositions) = run {
                    val outputByteArray = imageProcessor.blur(imageBytes, detected.positions)
                    val (name, extension) = imageUrl.split("/").last().split(".")
                    val blurredImageUrl = fileManagementService.uploadImage("${name}_b.$extension", outputByteArray)
                    blurredImageUrl to detected.positions
                }
                return@map BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = blurredImageUrl ?: imageUrl,
                    detectedPeopleCount = detectedPositions.size
                )
            } catch (e: Exception) {
                return@map BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = imageUrl,
                    detectedPeopleCount = 0
                )
            }
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