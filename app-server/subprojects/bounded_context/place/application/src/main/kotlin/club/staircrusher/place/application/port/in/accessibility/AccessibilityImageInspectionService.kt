package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.`in`.accessibility.image.ImageInspectionService
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.stdlib.di.annotation.Component

@Component
class AccessibilityImageInspectionService(
    private val imageInspectionService: ImageInspectionService,
) {
    suspend fun inspect(images: List<AccessibilityImage>): List<AccessibilityImage> {
        val (processed, unprocessed) = images.partition { it.inspectionResult != null }
        return unprocessed
            .let {
                val results = imageInspectionService.inspect(it.map { img -> img.thumbnailUrl ?: img.originalImageUrl })
                it.zip(results)
                    .map { (img, result) ->
                        img.inspectionResult = result.detectionResult
                        img
                    }
            } + processed
    }
}
