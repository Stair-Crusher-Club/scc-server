package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.image.ImageProcessor
import club.staircrusher.accessibility.application.port.out.DetectFacesService
import club.staircrusher.accessibility.application.port.out.file_management.FileManagementService
import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityImageFaceBlurringHistoryRepository
import club.staircrusher.accessibility.domain.model.AccessibilityImageFaceBlurringHistory
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
open class AccessibilityImageFaceBlurringService(
    private val accessibilityImageFaceBlurringHistoryRepository: AccessibilityImageFaceBlurringHistoryRepository,
    private val accessibilityImageService: AccessibilityImageService,
    private val imageProcessor: ImageProcessor,
    private val detectFacesService: DetectFacesService,
    private val fileManagementService: FileManagementService,
    private val transactionManager: TransactionManager,
) {
    suspend fun blurFacesInPlaceAccessibility(placeAccessibilityId: String) {
        val placeAccessibility = transactionManager.doInTransaction {
            accessibilityImageService.doMigratePlaceAccessibilityImageUrlsToImagesIfNeeded(placeAccessibilityId = placeAccessibilityId)
        }
        if (placeAccessibility == null) return
        val imageUrls = placeAccessibility.images.map { it.imageUrl }
        val result = detectAndBlurFaces(imageUrls)
        val blurredImages = result.filter { it.isBlurred() }
        transactionManager.doInTransaction {
            if (blurredImages.isEmpty()) {
                // 얼굴이 감지 되지 않으면 다음 accessibility 를 검사하기 위해 history 를 저장한다.
                accessibilityImageFaceBlurringHistoryRepository.save(
                    AccessibilityImageFaceBlurringHistory(
                        id = EntityIdGenerator.generateRandom(),
                        placeAccessibilityId = placeAccessibilityId, buildingAccessibilityId = null,
                        beforeImageUrl = null, afterImageUrl = null, detectedPeopleCount = 0,
                        createdAt = SccClock.instant(), updatedAt = SccClock.instant()
                    )
                )
            } else {
                val histories = blurredImages.map {
                    AccessibilityImageFaceBlurringHistory(
                        id = EntityIdGenerator.generateRandom(),
                        placeAccessibilityId = placeAccessibilityId, buildingAccessibilityId = null,
                        beforeImageUrl = it.originalImageUrl, afterImageUrl = it.blurredImageUrl,
                        detectedPeopleCount = it.detectedPeopleCount,
                        createdAt = SccClock.instant(), updatedAt = SccClock.instant()
                    )
                }
                accessibilityImageFaceBlurringHistoryRepository.saveAll(histories)
                accessibilityImageService.doUpdatePlaceAccessibilityOriginalImages(
                    placeAccessibilityId,
                    result.map { it.blurredImageUrl }
                )
            }
        }
    }

    suspend fun blurFacesInBuildingAccessibility(buildingAccessibilityId: String) {
        val buildingAccessibility = transactionManager.doInTransaction {
            accessibilityImageService.doMigrateBuildingAccessibilityImageUrlsToImagesIfNeeded(buildingAccessibilityId = buildingAccessibilityId)
        }
        if (buildingAccessibility == null) return
        val entranceResult = detectAndBlurFaces(buildingAccessibility.entranceImages.map { it.imageUrl })
        val elevatorResult = detectAndBlurFaces(buildingAccessibility.elevatorImages.map { it.imageUrl })
        val blurredImages = (entranceResult + elevatorResult).filter { it.isBlurred() }
        transactionManager.doInTransaction {
            if (blurredImages.isEmpty()) {
                // 얼굴이 감지 되지 않으면 다음 accessibility 를 검사하기 위해 history 를 저장한다.
                accessibilityImageFaceBlurringHistoryRepository.save(
                    AccessibilityImageFaceBlurringHistory(
                        id = EntityIdGenerator.generateRandom(),
                        placeAccessibilityId = null, buildingAccessibilityId = buildingAccessibilityId,
                        beforeImageUrl = null, afterImageUrl = null, detectedPeopleCount = 0,
                        createdAt = SccClock.instant(), updatedAt = SccClock.instant()
                    )
                )
            } else {
                val histories = blurredImages
                    .map {
                        AccessibilityImageFaceBlurringHistory(
                            id = EntityIdGenerator.generateRandom(),
                            placeAccessibilityId = null, buildingAccessibilityId = buildingAccessibilityId,
                            beforeImageUrl = it.originalImageUrl, afterImageUrl = it.blurredImageUrl,
                            detectedPeopleCount = it.detectedPeopleCount,
                            createdAt = SccClock.instant(), updatedAt = SccClock.instant()
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
    }

    private suspend fun detectAndBlurFaces(imageUrls: List<String>): List<BlurResult> {
        return imageUrls.map { imageUrl ->
            try {
                val detected = detectFacesService.detect(imageUrl)
                val imageBytes = detected.imageBytes
                if (detected.positions.isEmpty()) return@map BlurResult(
                    originalImageUrl = imageUrl,
                    blurredImageUrl = imageUrl,
                    detectedPeopleCount = 0
                )
                val (blurredImageUrl, detectedPositions) = run {
                    val outputByteArray = imageProcessor.blur(imageBytes, detected.positions)
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


    data class BlurResult(
        val originalImageUrl: String,
        val blurredImageUrl: String,
        val detectedPeopleCount: Int?,
    ) {
        fun isBlurred() = originalImageUrl != blurredImageUrl
    }
}
