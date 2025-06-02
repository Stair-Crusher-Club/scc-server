package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.image.application.port.out.file_management.FileManagementService
import club.staircrusher.place.application.port.`in`.accessibility.image.ThumbnailGenerator
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.stdlib.di.annotation.Component
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.nio.file.Files

@Component
class AccessibilityImageThumbnailService(
    private val thumbnailGenerator: ThumbnailGenerator,
    private val fileManagementService: FileManagementService,
) {
    private val logger = KotlinLogging.logger {}

    suspend fun generateThumbnails(accessibilityImages: List<AccessibilityImage>) = coroutineScope {
        accessibilityImages
            .map {
                async {
                    val thumbnail = generateThumbnails(it) ?: return@async null
                    val thumbnailUrl = fileManagementService.uploadThumbnailImage(
                        thumbnail.thumbnailFileName,
                        thumbnail.outputStream
                    )
                    it to thumbnailUrl
                }
            }
            .awaitAll()
            .filterNotNull()
            .map { (image, thumbnailUrl) ->
                if (thumbnailUrl != null) {
                    image.thumbnailUrl = thumbnailUrl
                }
                return@map image
            }
    }

    private suspend fun generateThumbnails(originalAccessibilityImage: AccessibilityImage): Thumbnail? {
        val originalImageUrl = originalAccessibilityImage.blurredImageUrl ?: originalAccessibilityImage.originalImageUrl
        val destinationPath = thumbnailPath.resolve(originalAccessibilityImage.id)
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
            logger.error(t) { "Failed to generate thumbnail for id: ${originalAccessibilityImage.id}, image: $originalImageUrl" }
            return null
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
