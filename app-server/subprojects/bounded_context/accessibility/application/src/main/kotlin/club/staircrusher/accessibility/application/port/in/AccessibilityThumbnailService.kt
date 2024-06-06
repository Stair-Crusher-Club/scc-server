package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.domain_event.AccessibilityThumbnailGeneratedEvent
import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.coobird.thumbnailator.Thumbnails
import java.nio.file.Files
import java.nio.file.Path

@Component
class AccessibilityThumbnailService(
    private val transactionManager: TransactionManager,
    private val domainEventPublisher: DomainEventPublisher,
    private val fileManagementService: FileManagementService,
    private val placeApplicationService: PlaceApplicationService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun generateThumbnailIfNotExists(placeId: String) {
        val (placeOriginalImageUrls, entranceOriginalImageUrls, elevatorOriginalImageUrls) = getOriginalImageUrls(placeId)
        if ((placeOriginalImageUrls + entranceOriginalImageUrls + elevatorOriginalImageUrls).isEmpty()) return

        if (placeOriginalImageUrls.isNotEmpty()) {
            registerPlaceAccessibilityThumbnail(placeId, placeOriginalImageUrls)
        }
        if (entranceOriginalImageUrls.isNotEmpty() || elevatorOriginalImageUrls.isNotEmpty()) {
            registerBuildingAccessibilityThumbnails(placeId, entranceOriginalImageUrls, elevatorOriginalImageUrls)
        }

        runBlocking { domainEventPublisher.publishEvent(AccessibilityThumbnailGeneratedEvent(thumbnailPath.resolve(placeId))) }
    }

    private fun getOriginalImageUrls(placeId: String) = transactionManager.doInTransaction {
        val place = placeApplicationService.findPlace(placeId) ?: return@doInTransaction Triple(emptyList(), emptyList(), emptyList())

        val placeAccessibility = placeAccessibilityRepository.findByPlaceId(placeId)
        val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.building.id)

        val shouldGeneratePAThumbnail = placeAccessibility != null && placeAccessibility.thumbnailUrls.isNullOrEmpty()
        val shouldGenerateBAThumbnail = buildingAccessibility != null &&
            (buildingAccessibility.entranceThumbnailUrls.isNullOrEmpty() || buildingAccessibility.elevatorThumbnailUrls.isNullOrEmpty())

        val placeOriginalImageUrls = placeAccessibility?.imageUrls?.takeIf { shouldGeneratePAThumbnail } ?: emptyList()
        val entranceOriginalImageUrls = buildingAccessibility?.entranceImageUrls.takeIf { shouldGenerateBAThumbnail } ?: emptyList()
        val elevatorOriginalImageUrls = buildingAccessibility?.elevatorImageUrls.takeIf { shouldGenerateBAThumbnail } ?: emptyList()

        return@doInTransaction Triple(placeOriginalImageUrls, entranceOriginalImageUrls, elevatorOriginalImageUrls)
    }

    private fun registerPlaceAccessibilityThumbnail(placeId: String, originalImageUrls: List<String>) {
        // 현재로서는 각 이미지에 대한 식별자가 없기 때문에 하나라도 실패하면 전체 실패로 처리
        val thumbnailImagePaths = originalImageUrls.mapNotNull { generateThumbnail(it, placeId) }
        if (thumbnailImagePaths.size != originalImageUrls.size) return

        val thumbnailUrls = uploadThumbnailImages(thumbnailImagePaths)
        if (thumbnailUrls.size != originalImageUrls.size) return

        transactionManager.doInTransaction(isolationLevel = TransactionIsolationLevel.SERIALIZABLE) {
            val placeAccessibility = placeAccessibilityRepository.findByPlaceId(placeId)!!
            placeAccessibilityRepository.updateThumbnailUrls(placeAccessibility.id, thumbnailUrls)
        }
    }

    private fun registerBuildingAccessibilityThumbnails(placeId: String, entranceImageUrls: List<String>, elevatorImageUrls: List<String>) {
        val entranceThumbnailImagePaths = entranceImageUrls.mapNotNull { generateThumbnail(it, placeId) }
        val elevatorThumbnailImagePaths = elevatorImageUrls.mapNotNull { generateThumbnail(it, placeId) }

        val entranceThumbnailUrls = if (entranceThumbnailImagePaths.size == entranceImageUrls.size) {
            uploadThumbnailImages(entranceThumbnailImagePaths)
        } else {
            emptyList()
        }
        val elevatorThumbnailUrls = if (elevatorThumbnailImagePaths.size == elevatorImageUrls.size) {
            uploadThumbnailImages(elevatorThumbnailImagePaths)
        } else {
            emptyList()
        }

        transactionManager.doInTransaction(isolationLevel = TransactionIsolationLevel.SERIALIZABLE) {
            val place = placeApplicationService.findPlace(placeId)
                ?: throw SccDomainException("Cannot find place with $placeId")
            val buildingAccessibility = buildingAccessibilityRepository.findByBuildingId(place.building.id)
                ?: throw SccDomainException("Cannot find building accessibility with ${place.building.id}")

            buildingAccessibilityRepository.updateThumbnailUrls(buildingAccessibility.id, entranceThumbnailUrls, elevatorThumbnailUrls)
        }
    }

    private fun generateThumbnail(originalImageUrl: String, placeId: String): Path? {
        try {
            val destinationPath = thumbnailPath.resolve(placeId)
            val imageFile = fileManagementService.downloadFile(originalImageUrl, destinationPath)
            val thumbnailFileName = "thumbnail_${imageFile.nameWithoutExtension}.$THUMBNAIL_FORMAT"
            val thumbnailFilePath = destinationPath.resolve(thumbnailFileName)

            val thumbnailOutputStream = Files.newOutputStream(thumbnailFilePath)
            Thumbnails.of(imageFile)
                .scale(0.33)
                .outputFormat(THUMBNAIL_FORMAT)
                .toOutputStream(thumbnailOutputStream)
            thumbnailOutputStream.close()

            return thumbnailFilePath
        } catch (t: Throwable) {
            logger.error(t) { "Failed to generate thumbnail for place: $placeId, image: $originalImageUrl" }
            return null
        }
    }

    private fun uploadThumbnailImages(imagePaths: List<Path>) = runBlocking {
        return@runBlocking imagePaths
            .map {
                val extension = THUMBNAIL_FORMAT
                val contentType = Files.probeContentType(it)
                // S3 에 dangling object 가 남을 수도 있을 것 같은데 괜찮은가?
                async { fileManagementService.uploadThumbnailImage(it, extension, contentType) }
            }
            .awaitAll()
            .filterNotNull()
    }

    companion object {
        private val thumbnailPath = Path.of("tmp", "thumbnails")
        private const val THUMBNAIL_FORMAT = "webp"
    }
}
