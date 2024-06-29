package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import java.util.concurrent.Executors


@Component
class BlurFacesInAccessibilityImagesUseCase(
) {
    private val taskExecutor = Executors.newCachedThreadPool()

    fun handleAsync(placeAccessibilityId: String) {
        taskExecutor.execute {
            handle(placeAccessibilityId)
        }
    }

    fun handle(placeAccessibilityId: String): List<String> {
        // Get image urls from PlaceAccessibilityRepository
//        val images = accessibilityImageService.getPlaceAccessibilityImages(placeAccessibilityId)
//        val imageUrls = images.map { it.imageUrl }
//        val result = imageUrls.map { imageUrl ->
//            try {
//                val (blurredImageUrl, detectedPositions) = URL(imageUrl).openStream().use { inputStream ->
//                    val imageBytes = inputStream.readBytes()
//                    val imageBytesPointer = BytePointer(*imageBytes)
//                    val detected = detectFacesService.detect(imageBytes)
//                    if (detected.positions.isEmpty()) return@map BlurResult(
//                        originalImageUrl = imageUrl,
//                        blurredImageUrl = imageUrl,
//                        detectedPeopleCount = 0
//                    )
//                    val outputByteArray = blur(imageBytesPointer, detected.positions)
//                    val (name, extension) = imageUrl.split("/").last().let { fileName ->
//                        fileName.split(".")
//                    }
//                    val blurredImageUrl = runBlocking {
//                        async { fileManagementService.uploadImage("${name}_b.$extension", outputByteArray) }.await()
//                    }
//                    blurredImageUrl to detected.positions
//                }
//                return@map BlurResult(
//                    originalImageUrl = imageUrl,
//                    blurredImageUrl = blurredImageUrl ?: imageUrl,
//                    detectedPeopleCount = detectedPositions.size
//                )
//            } catch (e: Exception) {
//                return@map BlurResult(
//                    originalImageUrl = imageUrl,
//                    blurredImageUrl = imageUrl,
//                    detectedPeopleCount = null
//                )
//            }
//        }
//        return transactionManager.doInTransaction {
//            result.filter { return@filter it.isBlurred() }.forEach {
//                accessibilityImagesBlurringHistoryRepository.save(
//                    AccessibilityImagesBlurringHistory(
//                        id = EntityIdGenerator.generateRandom(),
//                        placeAccessibilityId = placeAccessibilityId,
//                        buildingAccessibilityId = null,
//                        beforeImageUrl = it.originalImageUrl,
//                        afterImageUrl = it.blurredImageUrl,
//                        detectedPeopleCount = it.detectedPeopleCount,
//                        createdAt = SccClock.instant(),
//                        updatedAt = SccClock.instant()
//                    )
//                )
//            }
//            accessibilityImageService.doUpdatePlaceAccessibilityOriginalImages(
//                placeAccessibilityId,
//                result.map { it.blurredImageUrl }
//            )
//            return@doInTransaction result.map { return@map it.blurredImageUrl }
//        } ?: emptyList()
        return emptyList()
    }
}

