package club.staircrusher.testing.spring_it.mock

import club.staircrusher.accessibility.application.port.`in`.image.ImageProcessor
import club.staircrusher.accessibility.domain.model.DetectedFacePosition

class MockImageProcessor : ImageProcessor {
    override fun blur(originalImage: ByteArray, positions: List<DetectedFacePosition>): ByteArray {
        return originalImage
    }
}
