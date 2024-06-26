package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImagesBlurringHistoryRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImagesBlurringHistory
import club.staircrusher.stdlib.Rect
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
                val (blurredImageUrl, detectedPositions) = URL(imageUrl).openStream().use { inputStream ->
                    val imageBytes = inputStream.readBytes()
                    val imageBytesPointer = BytePointer(*imageBytes)
                    val detected = detectFacesService.detect(imageBytes)
                    if (detected.positions.isEmpty()) return@map BlurResult(
                        originalImageUrl = imageUrl,
                        blurredImageUrl = imageUrl,
                        detectedPeopleCount = 0
                    )
                    val outputByteArray = blur(imageBytesPointer, detected.positions)
                    imageUrl.split("/").last().let { fileName ->
                        val (name, extension) = fileName.split(".")
                        fileManagementService.upload("${name}_b", extension, outputByteArray)
                    } to detected.positions
                }
                return@map BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = blurredImageUrl,
                    detectedPeopleCount = detectedPositions.size
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

    private fun blur(originalImageBytePointer: BytePointer, positions: List<Rect>): ByteArray {
        val originalImageMat = imdecode(Mat(originalImageBytePointer), IMREAD_COLOR)
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

    data class BlurResult(
        val originalImageUrl: String,
        val blurredImageUrl: String,
        val detectedPeopleCount: Int?,
    ) {
        fun isBlurred() = originalImageUrl != blurredImageUrl
    }

    private fun Rect.toMatRect(): org.bytedeco.opencv.opencv_core.Rect {
        return org.bytedeco.opencv.opencv_core.Rect(this.x, this.y, this.width, this.height)
    }
}

