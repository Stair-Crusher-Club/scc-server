package club.staircrusher.testing.spring_it.mock

import club.staircrusher.place.application.port.`in`.accessibility.image.ImageInspectionService
import club.staircrusher.place.domain.model.check.ImageInspectionResult

class MockImageInspectionService : ImageInspectionService {
    override suspend fun inspect(imageUrls: List<String>): List<ImageInspectionService.Result> {
        return imageUrls.map {
            ImageInspectionService.Result(it, ImageInspectionResult.NotVisible)
        }
    }
}
