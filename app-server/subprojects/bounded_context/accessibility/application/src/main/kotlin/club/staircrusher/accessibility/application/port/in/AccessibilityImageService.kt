package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.image.ThumbnailGenerator
import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import kotlin.io.path.createDirectory

@Component
class AccessibilityImageService(
    private val transactionManager: TransactionManager,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val fileManagementService: FileManagementService,
    private val placeApplicationService: PlaceApplicationService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun migrateImageUrlsToImagesIfNeeded(placeId: String) = transactionManager.doInTransaction(isolationLevel = TransactionIsolationLevel.REPEATABLE_READ) {
        val place = placeApplicationService.findPlace(placeId) ?: return@doInTransaction
        doMigratePlaceAccessibilityImageUrlsToImagesIfNeeded(placeId = placeId)
        doMigrateBuildingAccessibilityImageUrlsToImagesIfNeeded(buildingId = place.building.id)
    }

    fun doMigratePlaceAccessibilityImageUrlsToImagesIfNeeded(
        placeId: String? = null,
        placeAccessibilityId: String? = null
    ): PlaceAccessibility? {
        val placeAccessibility =
            placeId?.let { placeAccessibilityRepository.findByPlaceId(it) }
                ?: placeAccessibilityId?.let { placeAccessibilityRepository.findById(it) }
                ?: return null
        if (placeAccessibility.images.isEmpty() && placeAccessibility.imageUrls.isNotEmpty()) {
            val placeAccessibilityImages = placeAccessibility.imageUrls.map { AccessibilityImage(imageUrl = it, thumbnailUrl = null) }
            placeAccessibilityRepository.updateImages(placeAccessibility.id, placeAccessibilityImages)
        }
        return placeId?.let { placeAccessibilityRepository.findByPlaceId(it) } ?: placeAccessibilityId?.let { placeAccessibilityRepository.findById(it) }
    }

    fun doMigrateBuildingAccessibilityImageUrlsToImagesIfNeeded(
        buildingId: String? = null,
        buildingAccessibilityId: String? = null,
    ): BuildingAccessibility? {
        val buildingAccessibility = buildingId?.let { buildingAccessibilityRepository.findByBuildingId(it) }
            ?: buildingAccessibilityId?.let { buildingAccessibilityRepository.findById(it) } ?: return null
        if (buildingAccessibility.entranceImages.isEmpty() && buildingAccessibility.entranceImageUrls.isNotEmpty()) {
            val buildingEntranceImages = buildingAccessibility.entranceImageUrls.map { AccessibilityImage(imageUrl = it, thumbnailUrl = null) }
            buildingAccessibilityRepository.updateEntranceImages(buildingAccessibility.id, buildingEntranceImages)
        }

        if (buildingAccessibility.elevatorImages.isEmpty() && buildingAccessibility.elevatorImageUrls.isNotEmpty()) {
            val buildingElevatorImages = buildingAccessibility.elevatorImageUrls.map { AccessibilityImage(imageUrl = it, thumbnailUrl = null) }
            buildingAccessibilityRepository.updateElevatorImages(buildingAccessibility.id, buildingElevatorImages)
        }

        return buildingId?.let { buildingAccessibilityRepository.findByBuildingId(it) } ?: buildingAccessibilityId?.let { buildingAccessibilityRepository.findById(it) }
    }

    fun generateThumbnailsIfNeeded(placeId: String) {
        val thumbnailGenerationRequiredImages = getThumbnailGenerationRequiredImages(placeId)
        val generatedThumbnailUrls = thumbnailGenerationRequiredImages
            .map { it.imageUrl }
            .mapNotNull { generateThumbnail(it, placeId) }
            .let { uploadThumbnailImages(it) }

        if (generatedThumbnailUrls.isNotEmpty()) {
            logger.info { "Saving generated thumbnail urls to DB" }
            saveThumbnailUrls(placeId, generatedThumbnailUrls)
        }
    }

    private fun getThumbnailGenerationRequiredImages(placeId: String) = transactionManager.doInTransaction {
        val place = placeApplicationService.findPlace(placeId) ?: return@doInTransaction emptyList()

        val placeAccessibility = placeAccessibilityRepository.findByPlaceId(placeId)
        val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.building.id)

        val accessibilityImages = listOfNotNull(placeAccessibility?.images, buildingAccessibility?.entranceImages, buildingAccessibility?.elevatorImages).flatten()

        return@doInTransaction accessibilityImages.filter { it.thumbnailUrl == null }
    }

    private fun saveThumbnailUrls(placeId: String, thumbnailUrls: List<String>) {
        transactionManager.doInTransaction(isolationLevel = TransactionIsolationLevel.REPEATABLE_READ) {
            val place = placeApplicationService.findPlace(placeId)!!
            val placeAccessibility = placeAccessibilityRepository.findByPlaceId(placeId)
            val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.building.id)

            if (placeAccessibility != null) {
                val updatedImages = placeAccessibility.images.map { image ->
                    findGeneratedThumbnailUrl(image.imageUrl, thumbnailUrls)?.let { image.thumbnailUrl = it }
                    image
                }

                placeAccessibilityRepository.updateImages(placeAccessibility.id, updatedImages)
            }

            if (buildingAccessibility != null) {
                val updatedEntranceImages = buildingAccessibility.entranceImages.map { image ->
                    findGeneratedThumbnailUrl(image.imageUrl, thumbnailUrls)?.let { image.thumbnailUrl = it }
                    image
                }
                buildingAccessibilityRepository.updateEntranceImages(buildingAccessibility.id, updatedEntranceImages)

                val updatedElevatorImages = buildingAccessibility.elevatorImages.map { image ->
                    findGeneratedThumbnailUrl(image.imageUrl, thumbnailUrls)?.let { image.thumbnailUrl = it }
                    image
                }
                buildingAccessibilityRepository.updateElevatorImages(buildingAccessibility.id, updatedElevatorImages)
            }
        }
    }

    private fun generateThumbnail(originalImageUrl: String, placeId: String): Thumbnail? {
        try {
            logger.info { "Generating thumbnail for place: $placeId, image: $originalImageUrl" }
            val destinationPath = thumbnailPath.resolve(placeId).createDirectory()
            val imageFile = fileManagementService.downloadFile(originalImageUrl, destinationPath)
            val thumbnailFileName = "thumbnail_${imageFile.nameWithoutExtension}.$THUMBNAIL_FORMAT"
            val thumbnailOutputStream = thumbnailGenerator.generate(imageFile, THUMBNAIL_FORMAT)

            logger.info { "Thumbnail thumbnail for place: $placeId, image: $originalImageUrl"}
            return Thumbnail(originalImageUrl, thumbnailFileName, thumbnailOutputStream)
        } catch (t: Throwable) {
            logger.error(t) { "Failed to generate thumbnail for place: $placeId, image: $originalImageUrl" }
            return null
        }
    }

    // FIXME: ThumbnailUploadResult 같은 data class를 만들어서 반환하면 findGeneratedThumbnailUrl에서 온몸비틀기를 안 해도 될지도?
    private fun uploadThumbnailImages(thumbnails: List<Thumbnail>) = runBlocking {
        logger.info { "Uploading thumbnails" }
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
