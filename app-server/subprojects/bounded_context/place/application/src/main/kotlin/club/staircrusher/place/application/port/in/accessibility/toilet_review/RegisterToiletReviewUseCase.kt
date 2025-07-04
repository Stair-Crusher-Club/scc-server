package club.staircrusher.place.application.port.`in`.accessibility.toilet_review

import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.port.out.accessibility.persistence.toilet_review.ToiletReviewRepository
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService

@Component
class RegisterToiletReviewUseCase(
    private val toiletReviewService: ToiletReviewService,
    private val userApplicationService: UserApplicationService,
    private val transactionManager: TransactionManager,
) {
    fun handle(params: ToiletReviewRepository.CreateParams) = transactionManager.doInTransaction {
        val userProfile = userApplicationService.getProfileByUserIdOrNull(params.userId)
            ?: throw SccDomainException("사용자를 찾을 수 없습니다.")
        val toiletReview = toiletReviewService.create(params)

        return@doInTransaction WithUserInfo(
            value = toiletReview,
            accessibilityRegisterer = userProfile.toDomainModel()
        )
    }
}
