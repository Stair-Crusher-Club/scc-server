package club.staircrusher.accessibility.application.port.`in`.image

import java.io.ByteArrayOutputStream
import java.io.File

interface ThumbnailGenerator {
    fun generate(originalImageFile: File, outputFormat: String): ByteArrayOutputStream
}
