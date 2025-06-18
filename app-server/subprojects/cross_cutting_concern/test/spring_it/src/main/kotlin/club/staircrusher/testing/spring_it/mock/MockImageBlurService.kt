package club.staircrusher.testing.spring_it.mock

import club.staircrusher.place.application.port.`in`.accessibility.image.ImageBlurService
import club.staircrusher.place.domain.model.accessibility.DetectedFacePosition


class MockImageBlurService : ImageBlurService {
    override fun blur(
        originalImage: ByteArray, imageExtension: String, positions: List<DetectedFacePosition>
    ): ByteArray {
        return originalImage
    }
}
