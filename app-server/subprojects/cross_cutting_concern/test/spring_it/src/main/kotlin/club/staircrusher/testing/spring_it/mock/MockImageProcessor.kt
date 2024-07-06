package club.staircrusher.testing.spring_it.mock

import club.staircrusher.accessibility.application.port.`in`.image.ImageProcessor
import club.staircrusher.stdlib.Rect

class MockImageProcessor : ImageProcessor {
    override fun blur(originalImage: ByteArray, positions: List<Rect>): ByteArray {
        return originalImage
    }
}
