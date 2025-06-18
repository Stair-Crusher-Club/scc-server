package club.staircrusher.place.application.port.`in`.accessibility.image

import club.staircrusher.place.domain.model.accessibility.DetectedFacePosition
import club.staircrusher.stdlib.Size

interface ImageFaceDetectionService {
    suspend fun detect(imageUrl: String): Result
    suspend fun detect(imageBytes: ByteArray): Result

    data class Result(
        val imageBytes: ByteArray,
        val imageSize: Size,
        val positions: List<DetectedFacePosition>,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Result

            if (!imageBytes.contentEquals(other.imageBytes)) return false
            if (imageSize != other.imageSize) return false
            if (positions != other.positions) return false

            return true
        }

        override fun hashCode(): Int {
            var result = imageBytes.contentHashCode()
            result = 31 * result + imageSize.hashCode()
            result = 31 * result + positions.hashCode()
            return result
        }
    }
}

