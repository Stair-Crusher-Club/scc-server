package club.staircrusher.place.application.port.`in`.accessibility.toilet_review

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.toilet_review.ToiletReviewRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.toilet_review.ToiletReview
import club.staircrusher.place.domain.model.accessibility.toilet_review.ToiletReviewDetail
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator

@Component
class ToiletReviewService(
    private val toiletReviewRepository: ToiletReviewRepository,
    private val accessibilityImageRepository: AccessibilityImageRepository,
) {
    fun create(params: ToiletReviewRepository.CreateParams): ToiletReview {
        val detail = if (params.floor != null && params.entranceDoorTypes.isNotEmpty() && params.imageUrls.isNotEmpty()) {
            ToiletReviewDetail(
                floor = params.floor,
                entranceDoorTypes = params.entranceDoorTypes,
                images = mutableListOf()
            )
        } else null

        val toiletReview = toiletReviewRepository.save(
            ToiletReview(
                id = EntityIdGenerator.generateRandom(),
                targetId = params.placeId,
                userId = params.userId,
                toiletLocationType = params.toiletLocationType,
                detail = detail,
                comment = params.comment,
            )
        ).also {
            if (it.detail != null) {
                it.detail!!.images = accessibilityImageRepository.saveAll(
                    params.imageUrls.mapIndexed { index, url ->
                        AccessibilityImage(
                            id = EntityIdGenerator.generateRandom(),
                            accessibilityId = it.id,
                            accessibilityType = AccessibilityImage.AccessibilityType.Toilet,
                            originalImageUrl = url,
                            displayOrder = index,
                        )
                    }
                ).toMutableList()
            }
        }

        return toiletReview
    }

    fun listByPlaceId(placeId: String): List<ToiletReview> {
        return toiletReviewRepository.findAllByTargetIdOrderByCreatedAtDesc(placeId)
    }
}
