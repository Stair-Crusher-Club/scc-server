package club.staircrusher.place.application.port.`in`.accessibility.toilet_review

import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityImageRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.toilet_review.ToiletReviewRepository
import club.staircrusher.place.domain.model.accessibility.AccessibilityImage
import club.staircrusher.place.domain.model.accessibility.toilet_review.ToiletReview
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import org.springframework.data.repository.findByIdOrNull

@Component
class ToiletReviewService(
    private val toiletReviewRepository: ToiletReviewRepository,
    private val accessibilityImageRepository: AccessibilityImageRepository,
) {
    fun create(params: ToiletReviewRepository.CreateParams): ToiletReview {
        val toiletReview = toiletReviewRepository.save(
            ToiletReview(
                id = EntityIdGenerator.generateRandom(),
                targetId = params.placeId,
                userId = params.userId,
                toiletLocationType = params.toiletLocationType,
                floor = params.floor,
                entranceDoorTypes = params.entranceDoorTypes,
                comment = params.comment,
            )
        ).also {
            it.images = accessibilityImageRepository.saveAll(
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

        return toiletReview
    }

    fun get(toiletReviewId: String): ToiletReview {
        return toiletReviewRepository.findByIdOrNull(toiletReviewId)
            ?: throw SccDomainException("화장실 리뷰를 찾을 수 없습니다")
    }

    fun listByPlaceId(placeId: String): List<ToiletReview> {
        return toiletReviewRepository.findAllByTargetIdOrderByCreatedAtDesc(placeId)
    }

    fun listByBuildingId(buildingId: String): List<ToiletReview> {
        return toiletReviewRepository.findAllByTargetIdOrderByCreatedAtDesc(buildingId)
    }

    fun delete(toiletReviewId: String) {
        return toiletReviewRepository.deleteById(toiletReviewId)
    }
}
