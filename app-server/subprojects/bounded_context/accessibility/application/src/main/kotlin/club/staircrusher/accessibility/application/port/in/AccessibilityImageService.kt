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

    fun generateThumbnailAndMigrateImagesIfNeeded(placeId: String) {
        val accessibilityImages = getAccessibilityImages(placeId)

        val thumbnailGenerationRequiredImages = accessibilityImages.filter { it.thumbnailUrl == null }
        val thumbnailUrls = thumbnailGenerationRequiredImages
            .map { it.imageUrl }
            .mapNotNull { generateThumbnail(it, placeId) }
            .let { uploadThumbnailImages(it) }

        migrateAccessibilityImagesIfNeeded(placeId, accessibilityImages, thumbnailUrls)
    }

    fun getPlaceAccessibilityImages(id: String) = transactionManager.doInTransaction {
        return@doInTransaction doGetPlaceAccessibilityImages(placeAccessibilityId = id)
    }

    fun getBuildingAccessibilityImages(id: String) = transactionManager.doInTransaction {
        return@doInTransaction doGetBuildingAccessibilityImages(buildingAccessibilityId = id)
    }

    fun doUpdatePlaceAccessibilityOriginalImages(
        placeAccessibilityId: String,
        originalImageUrls: List<String>
    ): PlaceAccessibility? {
        val placeAccessibility =
            placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId) ?: return null
        val newPlaceAccessibility = placeAccessibility.copy(
            imageUrls = originalImageUrls,
            images = originalImageUrls.map {
                AccessibilityImage(
                    type = AccessibilityImage.Type.PLACE,
                    imageUrl = it
                )
            }
        )
        return placeAccessibilityRepository.save(newPlaceAccessibility)
    }

    fun doUpdateBuildingAccessibilityOriginalImages(
        buildingAccessibilityId: String,
        originalEntranceImageUrls: List<String>,
        originalElevatorImageUrls: List<String>
    ): BuildingAccessibility? {
        val buildingAccessibility =
            buildingAccessibilityRepository.findByIdOrNull(buildingAccessibilityId) ?: return null
        val newBuildingAccessibility = buildingAccessibility.copy(
            entranceImageUrls = originalEntranceImageUrls,
            elevatorImageUrls = originalElevatorImageUrls,
            images = originalEntranceImageUrls.map {
                AccessibilityImage(type = AccessibilityImage.Type.BUILDING_ENTRANCE, imageUrl = it)
            } + originalElevatorImageUrls.map {
                AccessibilityImage(type = AccessibilityImage.Type.BUILDING_ELEVATOR, imageUrl = it)
            }

        )
        return buildingAccessibilityRepository.save(newBuildingAccessibility)
    }

    private fun getAccessibilityImages(placeId: String) = transactionManager.doInTransaction {
        val place = placeApplicationService.findPlace(placeId) ?: return@doInTransaction emptyList()
        val placeAccessibilityImages = doGetPlaceAccessibilityImages(placeId = placeId)
        val buildingAccessibilityImages = doGetBuildingAccessibilityImages(place.building.id)
        return@doInTransaction placeAccessibilityImages + buildingAccessibilityImages
    }

    private fun doGetPlaceAccessibilityImages(
        placeId: String? = null,
        placeAccessibilityId: String? = null
    ): List<AccessibilityImage> {
        val placeAccessibility =
            placeId?.let { placeAccessibilityRepository.findByPlaceId(it) }
                ?: placeAccessibilityId?.let { placeAccessibilityRepository.findById(it) }
                ?: return emptyList()
        val placeAccessibilityImages = placeAccessibility.images.ifEmpty {
            placeAccessibility.imageUrls.map { AccessibilityImage(AccessibilityImage.Type.PLACE, it, null) }
        }
        return placeAccessibilityImages
    }

    private fun doGetBuildingAccessibilityImages(
        buildingId: String? = null,
        buildingAccessibilityId: String? = null,
    ): List<AccessibilityImage> {
        val buildingAccessibility = buildingId?.let { buildingAccessibilityRepository.findByBuildingId(it) }
            ?: buildingAccessibilityId?.let { buildingAccessibilityRepository.findById(it) } ?: return emptyList()
        val buildingAccessibilityImages = buildingAccessibility.images.ifEmpty {
            val entranceImages = buildingAccessibility.entranceImageUrls.map {
                AccessibilityImage(
                    AccessibilityImage.Type.BUILDING_ENTRANCE,
                    it,
                    null
                )
            }
            val elevatorImages = buildingAccessibility.elevatorImageUrls.map {
                AccessibilityImage(
                    AccessibilityImage.Type.BUILDING_ELEVATOR,
                    it,
                    null
                )
            }
            entranceImages + elevatorImages
        }
        return buildingAccessibilityImages
    }

    private fun migrateAccessibilityImagesIfNeeded(
        placeId: String,
        originalAccessibilityImages: List<AccessibilityImage>,
        thumbnailUrls: List<String>
    ) {
        val updatedImages = originalAccessibilityImages.map {
            val originalFileNameWithoutExtension = it.imageUrl.split("/").last().split(".").first()
            val generatedThumbnailUrl =
                thumbnailUrls.firstOrNull { url -> url.contains(originalFileNameWithoutExtension) }
            if (generatedThumbnailUrl != null) {
                it.thumbnailUrl = generatedThumbnailUrl
            }

            it
        }

        val placeAccessibilityImages = updatedImages.filter { it.type == AccessibilityImage.Type.PLACE }
        val buildingAccessibilityImages =
            updatedImages.filter { it.type == AccessibilityImage.Type.BUILDING_ENTRANCE || it.type == AccessibilityImage.Type.BUILDING_ELEVATOR }

        transactionManager.doInTransaction(isolationLevel = TransactionIsolationLevel.REPEATABLE_READ) {
            val place = placeApplicationService.findPlace(placeId)!!
            val placeAccessibility = placeAccessibilityRepository.findByPlaceId(placeId)
            val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.building.id)

            if (placeAccessibility != null && placeAccessibility.images.equalsByContent(placeAccessibilityImages)
                    .not()
            ) {
                placeAccessibilityRepository.updateImages(placeAccessibility.id, placeAccessibilityImages)
            }
            if (buildingAccessibility != null && buildingAccessibility.images.equalsByContent(
                    buildingAccessibilityImages
                ).not()
            ) {
                buildingAccessibilityRepository.updateImages(buildingAccessibility.id, buildingAccessibilityImages)
            }
        }
    }

    private fun generateThumbnail(originalImageUrl: String, placeId: String): Thumbnail? {
        try {
            val destinationPath = thumbnailPath.resolve(placeId).createDirectory()
            val imageFile = fileManagementService.downloadFile(originalImageUrl, destinationPath)
            val thumbnailFileName = "thumbnail_${imageFile.nameWithoutExtension}.$THUMBNAIL_FORMAT"
            val thumbnailOutputStream = thumbnailGenerator.generate(imageFile, THUMBNAIL_FORMAT)

            return Thumbnail(originalImageUrl, thumbnailFileName, thumbnailOutputStream)
        } catch (t: Throwable) {
            logger.error(t) { "Failed to generate thumbnail for place: $placeId, image: $originalImageUrl" }
            return null
        }
    }

    private fun uploadThumbnailImages(thumbnails: List<Thumbnail>) = runBlocking {
        return@runBlocking thumbnails
            .map { (_, fileName, outputStream) ->
                async { fileManagementService.uploadThumbnailImage(fileName, outputStream) }
            }
            .awaitAll()
            .filterNotNull()
    }

    private fun List<AccessibilityImage>.equalsByContent(other: List<AccessibilityImage>): Boolean {
        if (this === other) return true
        if (this.size != other.size) return false

        return this.all { image ->
            val otherImage = other.firstOrNull { it.imageUrl == image.imageUrl }
            if (otherImage == null) return@all false

            image.thumbnailUrl == otherImage.thumbnailUrl
        }
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
