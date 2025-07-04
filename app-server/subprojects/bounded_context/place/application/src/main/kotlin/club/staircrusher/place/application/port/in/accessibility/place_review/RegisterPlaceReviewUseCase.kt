package club.staircrusher.place.application.port.`in`.accessibility.place_review

import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.port.out.accessibility.persistence.place_review.PlaceReviewRepository
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.place.domain.model.accessibility.place_review.PlaceReview
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.user.application.port.`in`.UserApplicationService

@Component
class RegisterPlaceReviewUseCase(
    private val placeReviewService: PlaceReviewService,
    private val userApplicationService: UserApplicationService,
) {
    fun handle(params: PlaceReviewRepository.CreateParams): WithUserInfo<PlaceReview> {
        val userProfile = userApplicationService.getProfileByUserIdOrNull(params.userId)
            ?: throw SccDomainException("사용자를 찾을 수 없습니다.")
        val placeReview = placeReviewService.create(params)

        return WithUserInfo(
            value = placeReview,
            accessibilityRegisterer = userProfile.toDomainModel()
        )
    }
}
