package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.image.ImageProcessor
import club.staircrusher.accessibility.domain.model.DetectedFacePosition
import club.staircrusher.stdlib.di.annotation.Component
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR
import org.bytedeco.opencv.global.opencv_imgcodecs.imdecode
import org.bytedeco.opencv.global.opencv_imgcodecs.imencode
import org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Size

@Component
class AccessibilityImageProcessor : ImageProcessor {
    override fun blur(originalImage: ByteArray, positions: List<DetectedFacePosition>): ByteArray {
        BytePointer(*originalImage).use { imagePointer ->
            val originalImageMat = imdecode(Mat(imagePointer), IMREAD_COLOR)
            // Blur images
            val blurredMat = originalImageMat.clone()
            for (position in positions) {
                val faceRegion = blurredMat.apply(position.toMatRect())
                GaussianBlur(
                    faceRegion,
                    faceRegion,
                    Size(0, 0), // sigmaX, sigmaY 에 의해서 결정 10.0
                    10.0
                )
            }
            // Convert the result back to byte array
            val outputPointer = BytePointer()
            imencode(".jpg", blurredMat, outputPointer)
            return ByteArray(outputPointer.limit().toInt()).apply { outputPointer.get(this) }
        }
    }

    private fun DetectedFacePosition.toMatRect(): org.bytedeco.opencv.opencv_core.Rect {
        return org.bytedeco.opencv.opencv_core.Rect(this.x, this.y, this.width, this.height)
    }
}
