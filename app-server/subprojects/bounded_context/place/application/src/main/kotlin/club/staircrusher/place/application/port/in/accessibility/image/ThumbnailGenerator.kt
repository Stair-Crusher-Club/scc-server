package club.staircrusher.place.application.port.`in`.accessibility.image

import java.io.ByteArrayOutputStream
import java.io.File

interface ThumbnailGenerator {
    fun generate(originalImageFile: File, outputFormat: String): ByteArrayOutputStream
}
