package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.image.application.port.out.file_management.FileManagementService
import club.staircrusher.place.application.port.`in`.accessibility.image.ThumbnailGenerator
import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.PlaceAccessibilityRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.nio.file.Files

@Component
class AccessibilityImageThumbnailService(
    private val transactionManager: TransactionManager,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val fileManagementService: FileManagementService,
    private val placeApplicationService: PlaceApplicationService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun generateThumbnailsIfNeeded(placeId: String) {
        val thumbnailGenerationRequiredImages = getThumbnailGenerationRequiredImages(placeId)
        val generatedThumbnailUrls = thumbnailGenerationRequiredImages
            .map { it.imageUrl }
            .mapNotNull { generateThumbnail(it, placeId) }
            .let { uploadThumbnailImages(it) }

        if (generatedThumbnailUrls.isNotEmpty()) {
            saveThumbnailUrls(placeId, generatedThumbnailUrls)
        }
    }

    private fun getThumbnailGenerationRequiredImages(placeId: String) = transactionManager.doInTransaction {
        val place = placeApplicationService.findPlace(placeId) ?: return@doInTransaction emptyList()

        val placeAccessibility = placeAccessibilityRepository.findFirstByPlaceIdAndDeletedAtIsNull(placeId)
        val buildingAccessibility = buildingAccessibilityRepository.findFirstByBuildingIdAndDeletedAtIsNull(place.building.id)

        val accessibilityImages = listOfNotNull(placeAccessibility?.images, buildingAccessibility?.entranceImages, buildingAccessibility?.elevatorImages).flatten()

        return@doInTransaction accessibilityImages.filter { it.thumbnailUrl == null }
    }

    private fun saveThumbnailUrls(placeId: String, thumbnailUrls: List<String>) {
        transactionManager.doInTransaction(isolationLevel = TransactionIsolationLevel.REPEATABLE_READ) {
            val place = placeApplicationService.findPlace(placeId)!!
            val placeAccessibility = placeAccessibilityRepository.findFirstByPlaceIdAndDeletedAtIsNull(placeId)
            val buildingAccessibility = buildingAccessibilityRepository.findFirstByBuildingIdAndDeletedAtIsNull(place.building.id)

            if (placeAccessibility != null) {
                val updatedImages = placeAccessibility.images.map { image ->
                    findGeneratedThumbnailUrl(image.imageUrl, thumbnailUrls)?.let { image.thumbnailUrl = it }
                    image
                }

                placeAccessibility.updateImages(updatedImages)
                placeAccessibilityRepository.save(placeAccessibility)
            }

            if (buildingAccessibility != null) {
                val updatedEntranceImages = buildingAccessibility.entranceImages.map { image ->
                    findGeneratedThumbnailUrl(image.imageUrl, thumbnailUrls)?.let { image.thumbnailUrl = it }
                    image
                }
                buildingAccessibility.updateEntranceImages(updatedEntranceImages)

                val updatedElevatorImages = buildingAccessibility.elevatorImages.map { image ->
                    findGeneratedThumbnailUrl(image.imageUrl, thumbnailUrls)?.let { image.thumbnailUrl = it }
                    image
                }
                buildingAccessibility.updateElevatorImages(updatedElevatorImages)

                buildingAccessibilityRepository.save(buildingAccessibility)
            }
        }
    }

    private fun generateThumbnail(originalImageUrl: String, placeId: String): Thumbnail? {
        val destinationPath = thumbnailPath.resolve(placeId)
        if (Files.notExists(destinationPath)) {
            try {
                Files.createDirectory(destinationPath)
            } catch (t: Throwable) {
                logger.error(t) { "Failed to create directory for thumbnail: $destinationPath" }
                return null
            }
        }

        try {
            val imageFile = fileManagementService.downloadFile(originalImageUrl, destinationPath)
            val thumbnailFileName = "thumbnail_${imageFile.nameWithoutExtension}.$THUMBNAIL_FORMAT"
            val thumbnailOutputStream = thumbnailGenerator.generate(imageFile, THUMBNAIL_FORMAT)

            return Thumbnail(originalImageUrl, thumbnailFileName, thumbnailOutputStream)
        } catch (t: Throwable) {
            logger.error(t) { "Failed to generate thumbnail for place: $placeId, image: $originalImageUrl" }
            return null
        }
    }

    // FIXME: ThumbnailUploadResult 같은 data class를 만들어서 반환하면 findGeneratedThumbnailUrl에서 온몸비틀기를 안 해도 될지도?
    private fun uploadThumbnailImages(thumbnails: List<Thumbnail>) = runBlocking {
        if (thumbnails.isEmpty()) return@runBlocking emptyList()
        return@runBlocking thumbnails
            .map { (_, fileName, outputStream) ->
                async { fileManagementService.uploadThumbnailImage(fileName, outputStream) }
            }
            .awaitAll()
            .filterNotNull()
    }

    private fun findGeneratedThumbnailUrl(originalImageUrl: String, thumbnailUrls: List<String>): String? {
        val originalFileNameWithoutExtension = originalImageUrl.split("/").last().split(".").first()
        return thumbnailUrls.firstOrNull { it.contains(originalFileNameWithoutExtension) }
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
