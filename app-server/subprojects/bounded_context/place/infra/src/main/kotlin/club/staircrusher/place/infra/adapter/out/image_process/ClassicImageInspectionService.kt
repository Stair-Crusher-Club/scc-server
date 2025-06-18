package club.staircrusher.place.infra.adapter.out.image_process

import club.staircrusher.stdlib.di.annotation.Component
import java.io.File
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.global.opencv_core
import org.opencv.core.CvType

@Component
class ClassicImageInspectionService {

    fun isImageQualityGood(file: File): Boolean {
        val image = opencv_imgcodecs.imread(file.absolutePath)
        if (image.empty()) return false

        val gray = Mat()
        opencv_imgproc.cvtColor(image, gray, opencv_imgproc.COLOR_BGR2GRAY)

        val laplacian = Mat()
        opencv_imgproc.Laplacian(gray, laplacian, CvType.CV_64F)

        val mean = opencv_core.mean(laplacian).get()
        return mean > 10 // Threshold for sharpness, tune as needed
    }
}
