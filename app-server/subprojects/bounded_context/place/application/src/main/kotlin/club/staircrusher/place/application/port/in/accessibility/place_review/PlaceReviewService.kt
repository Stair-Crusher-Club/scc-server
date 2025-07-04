package club.staircrusher.place.application.port.`in`.accessibility.place_review

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.place_review.PlaceReviewRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.place_review.PlaceReview
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator

@Component
class PlaceReviewService(
    private val placeReviewRepository: PlaceReviewRepository,
    private val accessibilityImageRepository: AccessibilityImageRepository,
) {
    fun create(params: PlaceReviewRepository.CreateParams): PlaceReview {
        val placeReview = placeReviewRepository.save(
            PlaceReview(
                placeId = params.placeId,
                userId = params.userId,
                recommendedMobilityTypes = params.recommendedMobilityTypes,
                spaciousType = params.spaciousType,
                comment = params.comment,
                mobilityTool = params.mobilityTool,
                seatTypes = params.seatTypes,
                orderMethods = params.orderMethods,
                features = params.features,
            )
        ).also {
            it.images = accessibilityImageRepository.saveAll(
                params.imageUrls.mapIndexed { idx, url ->
                    AccessibilityImage(
                        id = EntityIdGenerator.generateRandom(),
                        accessibilityId = it.id,
                        accessibilityType = AccessibilityImage.AccessibilityType.PlaceReview,
                        originalImageUrl = url,
                        displayOrder = idx,
                    )
                }
            ).toMutableList()
        }

        return placeReview
    }
}
