package club.staircrusher.place.application.port.`in`.accessibility.place_review

import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService

@Component
class ListPlaceReviewsUseCase(
    private val placeReviewService: PlaceReviewService,
    private val userApplicationService: UserApplicationService,
    private val transactionManager: TransactionManager,
) {
    fun handle(placeId: String) = transactionManager.doInTransaction(isReadOnly = true) {
        val placeReviews = placeReviewService.list(placeId)
        val idToReviewerMap = placeReviews.map { it.userId }.distinct()
            .let { userApplicationService.getProfilesByUserIds(it) }
            .associate { it.id to it.toDomainModel() }

        placeReviews.map {
            WithUserInfo(
                value = it,
                accessibilityRegisterer = idToReviewerMap[it.userId],
            )
        }
    }
}
