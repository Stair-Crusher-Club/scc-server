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
class AccessibilityImageBlurProcessor : ImageProcessor {
    private val logger = KotlinLogging.logger {}

    @Suppress("NestedBlockDepth")
    override fun blur(originalImage: ByteArray, imageExtension: String, positions: List<DetectedFacePosition>): ByteArray {
        BytePointer(*originalImage).use { imagePointer ->
            imdecode(Mat(imagePointer), IMREAD_COLOR).use { imageMat ->
                // Blur images
                for (position in positions) {
                    val faceRegion = try {
                        imageMat.apply(position.toMatRect())
                    } catch (t: Throwable) {
                        logger.error { "Failed to apply face region: $position for image size (${imageMat.size().width()}, ${imageMat.size().height()})" }
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
                    imencode(".$imageExtension", imageMat, outputPointer)
                    // JVM 입장에서 보면 Mat은 20byte 짜리 object 이지만, native C++ 코드 상에서 잡고 있는 메모리는 훨씬 크다
                    // use block 안에서 돌고 있지만 더욱 확실하게 Mat을 release 해줘서 메모리 leak을 방지한다.
                    imageMat.release()
                    return ByteArray(outputPointer.limit().toInt()).apply { outputPointer.get(this) }
                }
            }
        }
    }

    private fun DetectedFacePosition.toMatRect(): org.bytedeco.opencv.opencv_core.Rect {
        return org.bytedeco.opencv.opencv_core.Rect(this.x, this.y, this.width, this.height)
    }
}
