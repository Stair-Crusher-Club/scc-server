package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.FacePosition
import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImagesBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImagesBlurringHistory
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
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
    private val accessibilityImagesBlurringHistoryRepository: AccessibilityImagesBlurringHistoryRepository,
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

    fun handle(placeAccessibilityId: String): List<String> {
        // Get image urls from PlaceAccessibilityRepository
        val placeAccessibility = transactionManager.doInTransaction {
            placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId)
        }
        val imageUrls = placeAccessibility?.imageUrls
        if (imageUrls.isNullOrEmpty()) return emptyList()
        val result = imageUrls.map { imageUrl ->
            try {
                val imageBytes = URL(imageUrl).openStream().readBytes()
                val imageBytesPointer = BytePointer(*imageBytes)

                val detected = detectFacesService.detect(imageBytes)
                if (detected.positions.isEmpty()) return@map BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = imageUrl,
                    detectedPeopleCount = 0
                )
                val outputByteArray = blur(imageBytesPointer, detected.positions)
                val blurredImageUrl = imageUrl.split("/").last().let { fileName ->
                    val (name, extension) = fileName.split(".")
                    fileManagementService.upload("${name}_b", extension, outputByteArray)
                }
                return@map BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = blurredImageUrl,
                    detectedPeopleCount = detected.positions.size
                )
            } catch (e: Exception) {
                return@map BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = imageUrl,
                    detectedPeopleCount = null
                )
            }
        }
        return transactionManager.doInTransaction {
            result.filter { return@filter it.isBlurred() }.forEach {
                accessibilityImagesBlurringHistoryRepository.save(
                    AccessibilityImagesBlurringHistory(
                        id = EntityIdGenerator.generateRandom(),
                        placeAccessibilityId = placeAccessibilityId,
                        buildingAccessibilityId = null,
                        beforeImageUrl = it.originalImageUrl,
                        afterImageUrl = it.blurredImageUrl,
                        detectedPeopleCount = it.detectedPeopleCount,
                        createdAt = SccClock.instant(),
                        updatedAt = SccClock.instant()
                    )
                )
            }
            placeAccessibilityRepository.save(placeAccessibility.copy(imageUrls = result.map { it.blurredImageUrl }))
            return@doInTransaction result.map { return@map it.blurredImageUrl }
        } ?: emptyList()
    }

    private fun blur(originalImageBytePointer: BytePointer, positions: List<FacePosition>): ByteArray {
        val originalImageMat = imdecode(Mat(originalImageBytePointer), IMREAD_COLOR)
        // Blur images
        val blurredMat = originalImageMat.clone()
        for (position in positions) {
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
        return ByteArray(outputPointer.limit().toInt()).apply { outputPointer.get(this) }
    }

    data class BlurResult(
        val originalImageUrl: String,
        val blurredImageUrl: String,
        val detectedPeopleCount: Int?,
    ) {
        fun isBlurred() = originalImageUrl != blurredImageUrl
    }
}
