package club.staircrusher.accessibility.infra.adapter.out

import club.staircrusher.accessibility.application.port.out.DetectFacesResponse
import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.domain.model.DetectedFacePosition
import club.staircrusher.stdlib.Size
import club.staircrusher.stdlib.di.annotation.Component
import kotlinx.coroutines.future.await
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient
import software.amazon.awssdk.services.rekognition.model.Attribute
import software.amazon.awssdk.services.rekognition.model.BoundingBox
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest
import software.amazon.awssdk.services.rekognition.model.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.net.URL
import javax.imageio.ImageIO

@Component
internal class AwsRekognitionService(
    private val properties: RekognitionProperties,
) : DetectFacesService {
    private val rekognitionClient = RekognitionAsyncClient.builder()
        .region(Region.AP_NORTHEAST_2)
        .apply {
            properties.getAwsCredentials()?.let { credentialsProvider { it } }
        }
        .build()

    override suspend fun detect(imageUrl: String): DetectFacesResponse {
        val imageBytes = downloadImage(imageUrl)
        return detect(imageBytes)
    }

    override suspend fun detect(imageBytes: ByteArray): DetectFacesResponse {
        val imageSize = getImageSize(imageBytes)
        val detected = detectFacesFromBytes(imageBytes)
        return DetectFacesResponse(
            imageBytes = imageBytes,
            imageSize = imageSize,
            positions = detected.faceDetails().map {
                calculateFacePosition(getImageSize(imageBytes), it.boundingBox())
            },
        )
    }

    private suspend fun detectFacesFromBytes(imageBytes: ByteArray): software.amazon.awssdk.services.rekognition.model.DetectFacesResponse {
        val image = Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build()

        val request = DetectFacesRequest.builder()
            .image(image)
            .attributes(Attribute.ALL)
            .build()

        return rekognitionClient.detectFaces(request).await()
    }

    private fun calculateFacePosition(imageSize: Size, boundingBox: BoundingBox): DetectedFacePosition {
        val startX = (boundingBox.left() * imageSize.width).toInt()
        val startY = (boundingBox.top() * imageSize.height).toInt()
        val endX = (startX + boundingBox.width() * imageSize.width).toInt()
        val endY = (startY + boundingBox.height() * imageSize.height).toInt()
        return DetectedFacePosition(
            x = startX,
            y = startY,
            width = endX - startX,
            height = endY - startY,
        )
    }

    private fun getImageSize(imageBytes: ByteArray): Size {
        ByteArrayInputStream(imageBytes).use {
            val image: BufferedImage = ImageIO.read(it)
            return Size(width = image.width, height = image.height)
        }
    }

    private fun downloadImage(imageUrl: String): ByteArray {
        URL(imageUrl).openStream().use { inputStream ->
            return inputStream.readBytes()
        }
    }
}
