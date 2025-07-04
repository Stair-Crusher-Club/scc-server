package club.staircrusher.place.application.port.`in`.accessibility.toilet_review

import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.port.out.accessibility.persistence.toilet_review.ToiletReviewRepository
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.place.domain.model.accessibility.toilet_review.ToiletReview
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.user.application.port.`in`.UserApplicationService

@Component
class RegisterToiletReviewUseCase(
    private val toiletReviewService: ToiletReviewService,
    private val userApplicationService: UserApplicationService,
) {
    fun handle(params: ToiletReviewRepository.CreateParams): WithUserInfo<ToiletReview> {
        val userProfile = userApplicationService.getProfileByUserIdOrNull(params.userId)
            ?: throw SccDomainException("사용자를 찾을 수 없습니다.")
        val toiletReview = toiletReviewService.create(params)

        return WithUserInfo(
            value = toiletReview,
            accessibilityRegisterer = userProfile.toDomainModel()
        )
    }
}
