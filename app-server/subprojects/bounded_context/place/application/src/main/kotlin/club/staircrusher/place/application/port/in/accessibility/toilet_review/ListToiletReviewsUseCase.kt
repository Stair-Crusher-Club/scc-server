package club.staircrusher.place.application.port.`in`.accessibility.toilet_review

import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService

@Component
class ListToiletReviewsUseCase(
    private val toiletReviewService: ToiletReviewService,
    private val userApplicationService: UserApplicationService,
    private val transactionManager: TransactionManager,
) {
    fun handle(placeId: String) = transactionManager.doInTransaction(isReadOnly = true) {
        val toiletReviews = toiletReviewService.listByPlaceId(placeId)
        val idToReviewerMap = toiletReviews.map { it.userId }.distinct()
            .let { userApplicationService.getProfilesByUserIds(it) }
            .associate { it.id to it.toDomainModel() }

        toiletReviews.map {
            WithUserInfo(
                value = it,
                accessibilityRegisterer = idToReviewerMap[it.userId],
            )
        }
    }
}
