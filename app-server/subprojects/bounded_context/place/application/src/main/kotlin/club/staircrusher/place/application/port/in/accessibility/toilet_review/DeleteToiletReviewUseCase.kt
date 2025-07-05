package club.staircrusher.place.application.port.`in`.accessibility.toilet_review

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeleteToiletReviewUseCase(
    private val toiletReviewService: ToiletReviewService,
    private val transactionManager: TransactionManager,
) {
    fun handle(toiletReviewId: String, userId: String) = transactionManager.doInTransaction {
        val toiletReview = toiletReviewService.get(toiletReviewId)
        if (!toiletReview.isDeletable(userId)) {
            throw SccDomainException("해당 화장실 리뷰를 삭제할 권한이 없습니다.")
        }
        toiletReviewService.delete(toiletReviewId)
    }
}
