package club.staircrusher.accessibility.application.port.out

interface DetectFacesService {
    fun detect(imageUrl: String): Response
    fun detect(imageBytes: ByteArray): Response
}

data class Response(
    val imageSize: ImageSize,
    val positions: List<FacePosition>,
)

data class ImageSize(
    val width: Int,
    val height: Int,
)

data class FacePosition(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)
