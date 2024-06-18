package club.staircrusher.accessibility.application.port.out

import club.staircrusher.stdlib.Rect
import club.staircrusher.stdlib.Size

interface DetectFacesService {
    fun detect(imageUrl: String): DetectFacesResponse
    fun detect(imageBytes: ByteArray): DetectFacesResponse
}

data class DetectFacesResponse(
    val imageSize: Size,
    val positions: List<Rect>,
)
