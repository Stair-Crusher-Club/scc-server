package club.staircrusher.place.infra.adapter.out.image_process

import club.staircrusher.place.application.port.`in`.accessibility.image.ImageThumbnailService
import club.staircrusher.stdlib.di.annotation.Component
import net.coobird.thumbnailator.Thumbnails
import java.io.ByteArrayOutputStream
import java.io.File

@Component
class ImageThumbnailServiceImpl : ImageThumbnailService {

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
