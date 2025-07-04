package club.staircrusher.place.application.port.`in`.accessibility.place_review

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeletePlaceReviewUseCase(
    private val placeReviewService: PlaceReviewService,
    private val transactionManager: TransactionManager,
) {
    fun handle(placeReviewId: String, userId: String) = transactionManager.doInTransaction {
        val placeReview = placeReviewService.get(placeReviewId)
        if (!placeReview.isDeletable(userId)) {
            throw SccDomainException("해당 장소 리뷰를 삭제할 권한이 없습니다.")
        }
        placeReviewService.delete(placeReviewId)
    }
}
