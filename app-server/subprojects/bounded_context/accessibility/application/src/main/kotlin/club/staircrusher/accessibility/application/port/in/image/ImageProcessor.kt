package club.staircrusher.accessibility.application.port.`in`.image

import club.staircrusher.accessibility.domain.model.DetectedFacePosition

interface ImageProcessor {
    fun blur(originalImage: ByteArray, imageExtension: String, positions: List<DetectedFacePosition>): ByteArray
}
