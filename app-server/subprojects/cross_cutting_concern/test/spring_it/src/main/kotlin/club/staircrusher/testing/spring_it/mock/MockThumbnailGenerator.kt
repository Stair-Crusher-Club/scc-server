package club.staircrusher.testing.spring_it.mock

import club.staircrusher.accessibility.application.port.`in`.image.ThumbnailGenerator
import java.io.ByteArrayOutputStream
import java.io.File

class MockThumbnailGenerator : ThumbnailGenerator {
    override fun generate(originalImageFile: File, outputFormat: String): ByteArrayOutputStream {
        return ByteArrayOutputStream()
    }
}
