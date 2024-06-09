package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
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

@Component
class AccessibilityImageFaceBlurringService(
    private val accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository,
    private val accessibilityImageService: AccessibilityImageService,
    private val detectFacesService: DetectFacesService,
    private val fileManagementService: FileManagementService,
    private val transactionManager: TransactionManager,
) {
    suspend fun blurFacesInPlaceAccessibility(placeAccessibilityId: String) {
        val images = accessibilityImageService.getPlaceAccessibilityImages(placeAccessibilityId)
        val imageUrls = images.map { it.imageUrl }
        val result = detectAndBlurFaces(imageUrls)
        transactionManager.doInTransaction {
            val histories = result.filter { it.isBlurred() }.map {
                accessibilityImageFaceBlurringHistoryRepository.save(
                    AccessibilityImageFaceBlurringHistory(
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
            accessibilityImageFaceBlurringHistoryRepository.saveAll(histories)
            accessibilityImageService.doUpdatePlaceAccessibilityOriginalImages(
                placeAccessibilityId,
                result.map { it.blurredImageUrl }
            )
        }
    }

    suspend fun blurFacesInBuildingAccessibility(buildingAccessibilityId: String) {
        val images = accessibilityImageService.getBuildingAccessibilityImages(buildingAccessibilityId)
        val entranceImages = images.filter { it.type == AccessibilityImage.Type.BUILDING_ENTRANCE }.map { it.imageUrl }
        val entranceResult = detectAndBlurFaces(entranceImages)
        val elevatorImages = images.filter { it.type == AccessibilityImage.Type.BUILDING_ELEVATOR }.map { it.imageUrl }
        val elevatorResult = detectAndBlurFaces(elevatorImages)
        transactionManager.doInTransaction {
            val histories = (entranceResult + elevatorResult)
                .filter { it.isBlurred() }
                .map {
                    AccessibilityImageFaceBlurringHistory(
                        id = EntityIdGenerator.generateRandom(),
                        placeAccessibilityId = null,
                        buildingAccessibilityId = buildingAccessibilityId,
                        beforeImageUrl = it.originalImageUrl,
                        afterImageUrl = it.blurredImageUrl,
                        detectedPeopleCount = it.detectedPeopleCount,
                        createdAt = SccClock.instant(),
                        updatedAt = SccClock.instant()
                    )
                }
            accessibilityImageFaceBlurringHistoryRepository.saveAll(histories)
            accessibilityImageService.doUpdateBuildingAccessibilityOriginalImages(
                buildingAccessibilityId,
                entranceResult.map { it.blurredImageUrl },
                elevatorResult.map { it.blurredImageUrl }
            )
        }
    }

    suspend fun detectAndBlurFaces(imageUrls: List<String>): List<BlurResult> {
        return imageUrls.map { imageUrl ->
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
                    val outputByteArray = blurImage(imageBytesPointer, detected.positions)
                    val (name, extension) = imageUrl.split("/").last().split(".")
                    val blurredImageUrl = fileManagementService.uploadImage("${name}_b.$extension", outputByteArray)
                    blurredImageUrl to detected.positions
                }
                return@map BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = blurredImageUrl ?: imageUrl,
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
    }

    private fun blurImage(originalImageBytePointer: BytePointer, positions: List<Rect>): ByteArray {
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
