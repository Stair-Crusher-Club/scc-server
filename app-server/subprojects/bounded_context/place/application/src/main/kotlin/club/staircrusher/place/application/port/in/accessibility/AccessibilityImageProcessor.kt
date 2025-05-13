package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.image.ImageProcessor
import club.staircrusher.place.domain.model.accessibility.DetectedFacePosition
import club.staircrusher.stdlib.di.annotation.Component
import mu.KotlinLogging
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR
import org.bytedeco.opencv.global.opencv_imgcodecs.imdecode
import org.bytedeco.opencv.global.opencv_imgcodecs.imencode
import org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size

@Component
class AccessibilityImageProcessor : ImageProcessor {
    private val logger = KotlinLogging.logger {}

    @Suppress("NestedBlockDepth")
    override fun blur(originalImage: ByteArray, imageExtension: String, positions: List<DetectedFacePosition>): ByteArray {
        BytePointer(*originalImage).use { imagePointer ->
            imdecode(Mat(imagePointer), IMREAD_COLOR).use { originalImageMat ->
                originalImageMat.clone().use { blurredMat ->
                    // Blur images
                    for (position in positions) {
                        val faceRegion = try {
                            blurredMat.apply(position.toMatRect())
                        } catch (t: Throwable) {
                            logger.error { "Failed to apply face region: $position for image size (${originalImageMat.size().width()}, ${originalImageMat.size().height()})" }
                            logger.error { t.message }
                            continue
                        }
                        GaussianBlur(
                            faceRegion,
                            faceRegion,
                            Size(0, 0), // sigmaX, sigmaY 에 의해서 결정 10.0
                            10.0,
                        )
                    }

                    // Convert the result back to byte array
                    BytePointer().use { outputPointer ->
                        imencode(".$imageExtension", blurredMat, outputPointer)
                        return ByteArray(outputPointer.limit().toInt()).apply { outputPointer.get(this) }
                    }
                }
            }
        }
    }

    private fun DetectedFacePosition.toMatRect(): org.bytedeco.opencv.opencv_core.Rect {
        return org.bytedeco.opencv.opencv_core.Rect(this.x, this.y, this.width, this.height)
    }
}
