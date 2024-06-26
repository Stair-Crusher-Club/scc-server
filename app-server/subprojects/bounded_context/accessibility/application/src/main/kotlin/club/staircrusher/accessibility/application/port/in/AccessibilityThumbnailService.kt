package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createDirectory

@Component
open class AccessibilityThumbnailService(
    private val transactionManager: TransactionManager,
    private val fileManagementService: FileManagementService,
    private val placeApplicationService: PlaceApplicationService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun generateThumbnailIfNotExists(placeId: String) {
        val accessibilityImages = getAccessibilityImages(placeId)

        val thumbnailGenerationRequiredImages = accessibilityImages.filter { it.thumbnailUrl == null }
        val thumbnails = thumbnailGenerationRequiredImages.mapNotNull { generateThumbnail(it.imageUrl, placeId) }
        val thumbnailUrls = uploadThumbnailImages(thumbnails)

        migrateAccessibilityImages(placeId, accessibilityImages, thumbnailUrls)
    }

    private fun getAccessibilityImages(placeId: String) = transactionManager.doInTransaction {
        val place = placeApplicationService.findPlace(placeId) ?: return@doInTransaction emptyList()

        val placeAccessibility = placeAccessibilityRepository.findByPlaceId(placeId)
        val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.building.id)

        val placeAccessibilityImages = if (placeAccessibility?.images?.isNotEmpty() == true) {
            placeAccessibility.images
        } else {
            placeAccessibility?.imageUrls?.map { AccessibilityImage(AccessibilityImage.Type.PLACE, it, null) } ?: emptyList()
        }
        val buildingAccessibilityImages = if (buildingAccessibility?.images?.isNotEmpty() == true) {
            buildingAccessibility.images
        } else {
            val entranceImages = buildingAccessibility?.entranceImageUrls?.map { AccessibilityImage(AccessibilityImage.Type.BUILDING_ENTRANCE, it, null) } ?: emptyList()
            val elevatorImages = buildingAccessibility?.elevatorImageUrls?.map { AccessibilityImage(AccessibilityImage.Type.BUILDING_ELEVATOR, it, null) } ?: emptyList()
            entranceImages + elevatorImages
        }

        return@doInTransaction placeAccessibilityImages + buildingAccessibilityImages
    }

    private fun migrateAccessibilityImages(placeId: String, originalAccessibilityImages: List<AccessibilityImage>, thumbnailUrls: List<String>) {
        val updatedImages = originalAccessibilityImages.map {
            val originalImageFileName = it.imageUrl.split("/").last()
            val generatedThumbnailUrl = thumbnailUrls.firstOrNull { url -> url.contains(originalImageFileName) }
            if (generatedThumbnailUrl != null) {
                it.thumbnailUrl = generatedThumbnailUrl
            }

            it
        }

        val placeAccessibilityImages = updatedImages.filter { it.type == AccessibilityImage.Type.PLACE }
        val buildingAccessibilityImages = updatedImages.filter { it.type == AccessibilityImage.Type.BUILDING_ENTRANCE || it.type == AccessibilityImage.Type.BUILDING_ELEVATOR }

        transactionManager.doInTransaction(isolationLevel = TransactionIsolationLevel.REPEATABLE_READ) {
            val place = placeApplicationService.findPlace(placeId)!!
            val placeAccessibility = placeAccessibilityRepository.findByPlaceId(placeId)!!
            val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.building.id)!!
            placeAccessibilityRepository.updateImages(placeAccessibility.id, placeAccessibilityImages)
            buildingAccessibilityRepository.updateImages(buildingAccessibility.id, buildingAccessibilityImages)
        }
    }

    private fun generateThumbnail(originalImageUrl: String, placeId: String): Thumbnail? {
        try {
            val destinationPath = thumbnailPath.resolve(placeId).createDirectory()
            val imageFile = fileManagementService.downloadFile(originalImageUrl, destinationPath)
            val thumbnailFileName = "thumbnail_${imageFile.nameWithoutExtension}.$THUMBNAIL_FORMAT"
            val byteArrayOutputStream = doGenerateThumbnail(imageFile)

            return Thumbnail(originalImageUrl, thumbnailFileName, byteArrayOutputStream)
        } catch (t: Throwable) {
            logger.error(t) { "Failed to generate thumbnail for place: $placeId, image: $originalImageUrl" }
            return null
        }
    }

    private fun doGenerateThumbnail(imageFile: File): ByteArrayOutputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.use {
            Thumbnails.of(imageFile)
                .scale(0.33)
                .outputFormat(THUMBNAIL_FORMAT)
                .toOutputStream(it)
        }

        return byteArrayOutputStream
    }

    private fun uploadThumbnailImages(thumbnails: List<Thumbnail>) = runBlocking {
        return@runBlocking thumbnails
            .map { (_, fileName, outputStream) ->
                async { fileManagementService.uploadThumbnailImage(fileName, outputStream) }
            }
            .awaitAll()
            .filterNotNull()
    }

    private data class Thumbnail(
        val originalImageUrl: String,
        val thumbnailFileName: String,
        val outputStream: ByteArrayOutputStream,
    )

    companion object {
        private val thumbnailPath = Files.createTempDirectory("thumbnails")
        private const val THUMBNAIL_FORMAT = "webp"
    }
}
