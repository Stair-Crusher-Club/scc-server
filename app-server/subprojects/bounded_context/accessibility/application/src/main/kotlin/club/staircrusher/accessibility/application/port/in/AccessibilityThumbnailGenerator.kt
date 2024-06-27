package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.image.ThumbnailGenerator
import club.staircrusher.stdlib.di.annotation.Component
import net.coobird.thumbnailator.Thumbnails
import java.io.ByteArrayOutputStream
import java.io.File

@Component
class AccessibilityThumbnailGenerator : ThumbnailGenerator {

    override fun generate(originalImageFile: File, outputFormat: String): ByteArrayOutputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        byteArrayOutputStream.use {
            Thumbnails.of(originalImageFile)
                .scale(0.33)
                .outputFormat(outputFormat)
                .toOutputStream(it)
        }

        return byteArrayOutputStream
    }
}
