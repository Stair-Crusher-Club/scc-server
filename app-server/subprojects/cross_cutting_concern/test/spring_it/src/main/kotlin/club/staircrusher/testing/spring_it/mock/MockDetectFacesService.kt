package club.staircrusher.testing.spring_it.mock

import club.staircrusher.accessibility.application.port.out.DetectFacesResponse
import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.stdlib.Rect
import club.staircrusher.stdlib.Size

class MockDetectFacesService : DetectFacesService {
    override suspend fun detect(imageUrl: String): DetectFacesResponse {
        return DetectFacesResponse(
            imageBytes = if (imageUrl == URL_WITH_FACES) byteArrayWithFaces else ByteArray(0),
            imageSize = Size(100, 100),
            positions = if (imageUrl == URL_WITH_FACES) listOf(Rect(30, 30, 10, 10)) else emptyList()
        )
    }

    override suspend fun detect(imageBytes: ByteArray): DetectFacesResponse {
        return DetectFacesResponse(
            imageBytes = imageBytes,
            imageSize = Size(100, 100),
            positions = if (imageBytes.contentEquals(byteArrayWithFaces)) listOf(Rect(0, 0, 100, 100)) else emptyList()
        )
    }

    companion object {
        const val URL_WITH_FACES = "https://staircrusher.club/faces.jpg"
        const val BLURRED_URL_WITH_FACES = "https://staircrusher.club/faces_b.jpg"
        const val URL_WITHOUT_FACES = "https://staircrusher.club/without_faces.jpg"
        val byteArrayWithFaces = ByteArray(100 * 100 * 3) { it.toByte() }
    }
}
