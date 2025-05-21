package club.staircrusher.place.application.port.`in`.accessibility.image

import club.staircrusher.place.domain.model.check.ImageInspectionResult

interface ImageInspectionService {
    suspend fun inspect(imageUrls: List<String>): List<Result>

    data class Result(
        val url: String,
        val detectionResult: ImageInspectionResult,
    )
}
