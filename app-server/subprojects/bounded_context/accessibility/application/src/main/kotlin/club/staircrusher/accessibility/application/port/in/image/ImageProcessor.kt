package club.staircrusher.accessibility.application.port.`in`.image

import club.staircrusher.stdlib.Rect

interface ImageProcessor {
    fun blur(originalImage: ByteArray, positions: List<Rect>): ByteArray
}
