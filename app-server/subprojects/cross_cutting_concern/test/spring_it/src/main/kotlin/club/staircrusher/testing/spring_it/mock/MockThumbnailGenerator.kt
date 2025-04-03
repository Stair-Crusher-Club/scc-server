package club.staircrusher.testing.spring_it.mock

import club.staircrusher.place.application.port.`in`.accessibility.image.ThumbnailGenerator
import java.io.ByteArrayOutputStream
import java.io.File

class MockThumbnailGenerator : ThumbnailGenerator {
    override fun generate(originalImageFile: File, outputFormat: String): ByteArrayOutputStream {
        val byteArrayOutputStream = ByteArrayOutputStream()
        originalImageFile.inputStream().use {
            it.copyTo(byteArrayOutputStream)
        }
        return byteArrayOutputStream
    }
}
