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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
//        val imageUrls = transactionManager.doInTransaction {
//            placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId)?.imageUrls
//        }
//        if (imageUrls.isNullOrEmpty()) return
        val imageUrls = listOf(
            "https://scc-dev-accessibility-images-2.s3.ap-northeast-2.amazonaws.com/20230816033619_995EB2AA6ECA40E5.jpeg",
            "https://scc-dev-accessibility-images-2.s3.ap-northeast-2.amazonaws.com/20230819085338_DCB16DBB939D4DEB.jpeg",
            "https://scc-dev-accessibility-images-2.s3.ap-northeast-2.amazonaws.com/20230827070328_CE07F758350F4775.jpeg"
        )
        imageUrls.forEach { imageUrl ->
            val imageBytes = java.net.URL(imageUrl).readBytes()
            val detected = detectFacesService.detect(imageBytes)
            if (detected.positions.isEmpty()) return
            val mat = imdecode(Mat(BytePointer(*imageBytes)), IMREAD_COLOR)
            // Blur images
            for (position in detected.positions) {
                val faceROI = Mat(mat, Rect(position.x, position.y, position.width, position.height))
                GaussianBlur(
                    faceROI,
                    faceROI,
                    Size(0, 0), // sigmaX, sigmaY 에 의해서 결정
                    10.0
                )
            }
            // Convert the result back to byte array
            val outputStream = ByteArrayOutputStream()
            imencode(".jpg", mat, outputStream)
            saveByteArrayToFile(outputStream.toByteArray(), "./test.jpg")
//            imageUrl.split("/").last().let { fileName ->
//                val (name, extension) = fileName.split(".")
//                fileManagementService.upload(name, extension, outputStream)
//            }
            // TODO: Update place accessibility
        }
    }

    fun saveByteArrayToFile(byteArray: ByteArray, filePath: String): File {
        val file = File(filePath)
        try {
            // Create a new file if it doesn't exist
            if (!file.exists()) {
                file.createNewFile()
            }

            // Write the byte array to the file
            val fos = FileOutputStream(file)
            fos.write(byteArray)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }
}
