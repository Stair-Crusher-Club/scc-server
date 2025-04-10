package club.staircrusher.place.application.port.out.accessibility

import club.staircrusher.place.domain.model.accessibility.DetectedFacePosition
import club.staircrusher.stdlib.Size

interface DetectFacesService {
    suspend fun detect(imageUrl: String): DetectFacesResponse
    suspend fun detect(imageBytes: ByteArray): DetectFacesResponse
}

data class DetectFacesResponse(
    val imageBytes: ByteArray,
    val imageSize: Size,
    val positions: List<DetectedFacePosition>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DetectFacesResponse

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
