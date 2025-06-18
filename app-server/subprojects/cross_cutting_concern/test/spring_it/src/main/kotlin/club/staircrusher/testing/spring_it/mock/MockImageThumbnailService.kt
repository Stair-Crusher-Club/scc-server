package club.staircrusher.testing.spring_it.mock

import club.staircrusher.place.application.port.`in`.accessibility.image.ImageThumbnailService
import java.io.ByteArrayOutputStream
import java.io.File

class MockImageThumbnailService : ImageThumbnailService {
    override fun generate(originalImageFile: File, outputFormat: String): ByteArrayOutputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        originalImageFile.inputStream().use {
            it.copyTo(byteArrayOutputStream)
        }
        return byteArrayOutputStream
    }
}
