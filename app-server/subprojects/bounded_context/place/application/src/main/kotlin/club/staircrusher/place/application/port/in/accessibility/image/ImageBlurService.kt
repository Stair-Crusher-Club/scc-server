package club.staircrusher.place.application.port.`in`.accessibility.image

import club.staircrusher.place.domain.model.accessibility.DetectedFacePosition


interface ImageBlurService {
    fun blur(originalImage: ByteArray, imageExtension: String, positions: List<DetectedFacePosition>): ByteArray
}
