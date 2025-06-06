package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.image.application.port.out.file_management.FileManagementService
import club.staircrusher.place.application.port.`in`.accessibility.image.ThumbnailGenerator
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.stdlib.di.annotation.Component
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

    suspend fun generateThumbnail(accessibilityImage: AccessibilityImage) = coroutineScope {
        val thumbnail = doGenerateThumbnail(accessibilityImage) ?: return@coroutineScope accessibilityImage
        val thumbnailUrl = fileManagementService.uploadThumbnailImage(
            thumbnail.thumbnailFileName,
            thumbnail.outputStream
        )

        if (thumbnailUrl != null) {
            accessibilityImage.thumbnailUrl = thumbnailUrl
        }
        return@coroutineScope accessibilityImage
    }

    private suspend fun doGenerateThumbnail(originalAccessibilityImage: AccessibilityImage): Thumbnail? {
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
