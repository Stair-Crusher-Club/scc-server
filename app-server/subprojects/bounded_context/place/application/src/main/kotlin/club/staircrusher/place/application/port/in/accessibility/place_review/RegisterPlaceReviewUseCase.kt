package club.staircrusher.place.application.port.`in`.accessibility.place_review

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.port.out.accessibility.persistence.place_review.PlaceReviewRepository
import club.staircrusher.place.application.port.out.place.persistence.PlaceRepository
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService

@Component
class RegisterPlaceReviewUseCase(
    private val placeReviewService: PlaceReviewService,
    private val userApplicationService: UserApplicationService,
    private val transactionManager: TransactionManager,
    private val challengeService: ChallengeService,
    private val placeRepository: PlaceRepository,
) {
    fun handle(params: PlaceReviewRepository.CreateParams) = transactionManager.doInTransaction {
        val userProfile = userApplicationService.getProfileByUserIdOrNull(params.userId)
            ?: throw SccDomainException("사용자를 찾을 수 없습니다.")
        val placeReview = placeReviewService.create(params)

        // 장소 정보 조회
        val place = placeRepository.findById(params.placeId).orElse(null)
            ?: throw SccDomainException("장소를 찾을 수 없습니다.")

        // 챌린지 기여도 업데이트
        challengeService.contributeToSatisfiedChallenges(
            userId = params.userId,
            contribution = ChallengeService.Contribution.PlaceReview(
                placeReviewId = placeReview.id,
                placeReviewAddress = ChallengeAddress(place.building),
                placeCategoryValue = place.category?.name,
            )
        )

        return@doInTransaction WithUserInfo(
            value = placeReview,
            accessibilityRegisterer = userProfile.toDomainModel()
        )
    }
}
