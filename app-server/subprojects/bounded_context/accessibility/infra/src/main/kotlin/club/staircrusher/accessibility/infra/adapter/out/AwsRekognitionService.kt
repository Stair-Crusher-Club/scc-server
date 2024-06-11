package club.staircrusher.accessibility.infra.adapter.out

import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.FacePosition
import club.staircrusher.accessibility.application.port.out.ImageSize
import club.staircrusher.accessibility.application.port.out.Response
import club.staircrusher.accessibility.infra.adapter.out.file_management.S3ImageUploadProperties
import club.staircrusher.stdlib.di.annotation.Component
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.rekognition.RekognitionClient
import software.amazon.awssdk.services.rekognition.model.Attribute
import software.amazon.awssdk.services.rekognition.model.BoundingBox
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse
import software.amazon.awssdk.services.rekognition.model.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@Component
internal class AwsRekognitionService(
    private val properties: S3ImageUploadProperties,
) : DetectFacesService {
    private val rekognitionClient = RekognitionClient.builder()
        .region(Region.AP_NORTHEAST_2)
        .apply {
            properties.getAwsCredentials()?.let { credentialsProvider { it } }
        }
        .build()

    override fun detect(imageUrl: String): Response {
        val imageBytes = downloadImage(imageUrl)
        return detect(imageBytes)
    }

    override fun detect(imageBytes: ByteArray): Response {
        val imageSize = getImageSize(imageBytes)
        val detected = detectFacesFromBytes(imageBytes)
        return Response(
            imageSize = imageSize,
            positions = detected.faceDetails().map {
                calculateFacePosition(getImageSize(imageBytes), it.boundingBox())
            },
        )
    }

    private fun detectFacesFromBytes(imageBytes: ByteArray): DetectFacesResponse {
        val image = Image.builder().bytes(SdkBytes.fromByteArray(imageBytes)).build()

        val request = DetectFacesRequest.builder()
            .image(image)
            .attributes(Attribute.ALL)
            .build()

        return rekognitionClient.detectFaces(request)
    }

    private fun calculateFacePosition(imageSize: ImageSize, boundingBox: BoundingBox): FacePosition {
        val startX = (boundingBox.left() * imageSize.width).toInt()
        val startY = (boundingBox.top() * imageSize.height).toInt()
        val endX = (startX + boundingBox.width() * imageSize.width).toInt()
        val endY = (startY + boundingBox.height() * imageSize.height).toInt()
        return FacePosition(
            x = startX,
            y = startY,
            width = endX - startX,
            height = endY - startY,
        )
    }

    private fun getImageSize(imageBytes: ByteArray): ImageSize {
        ByteArrayInputStream(imageBytes).use {
            val image: BufferedImage = ImageIO.read(it)
            return ImageSize(width = image.width, height = image.height)
        }
    }

    private fun downloadImage(imageUrl: String): ByteArray {
        val url = java.net.URL(imageUrl)
        return url.readBytes()
    }
}

