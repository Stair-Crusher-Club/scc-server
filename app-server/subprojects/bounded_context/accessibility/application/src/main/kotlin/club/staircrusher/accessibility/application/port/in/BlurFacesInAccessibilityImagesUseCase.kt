package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_COLOR
import org.bytedeco.opencv.global.opencv_imgcodecs.imdecode
import org.bytedeco.opencv.global.opencv_imgcodecs.imencode
import org.bytedeco.opencv.global.opencv_imgproc.GaussianBlur
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Rect
import org.bytedeco.opencv.opencv_core.Size
import java.net.URL
import java.util.concurrent.Executors


@Component
class BlurFacesInAccessibilityImagesUseCase(
    private val detectFacesService: DetectFacesService,
    private val fileManagementService: FileManagementService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val transactionManager: TransactionManager,
) {
    private val taskExecutor = Executors.newCachedThreadPool()

    fun handleAsync(placeAccessibilityId: String) {
        taskExecutor.execute {
            handle(placeAccessibilityId)
        }
    }

    fun handle(placeAccessibilityId: String) {
        // Get image urls from PlaceAccessibilityRepository
        val placeAccessibility = transactionManager.doInTransaction {
            placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId)
        }
        val imageUrls = placeAccessibility?.imageUrls
        if (imageUrls.isNullOrEmpty()) return
        val blurredImageUrls = imageUrls.map { imageUrl ->
            val imageBytes = URL(imageUrl).openStream().readBytes()
            val imageBytesPointer = BytePointer(*imageBytes)

            val detected = detectFacesService.detect(imageBytes)
            if (detected.positions.isEmpty()) return@map imageUrl

            val originalImageMat = imdecode(Mat(imageBytesPointer), IMREAD_COLOR)
            // Blur images
            val blurredMat = originalImageMat.clone()
            for (position in detected.positions) {
                val faceRegion = blurredMat.apply(Rect(position.x, position.y, position.width, position.height))
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
            val outputByteArray = ByteArray(outputPointer.limit().toInt()).apply { outputPointer.get(this) }
            val blurredImageUrl = imageUrl.split("/").last().let { fileName ->
                val (name, extension) = fileName.split(".")
                fileManagementService.upload("${name}_b", extension, outputByteArray)
            }
            blurredImageUrl
        }
        transactionManager.doInTransaction {
            placeAccessibilityRepository.save(placeAccessibility.copy(imageUrls = blurredImageUrls))
        }
    }
}
