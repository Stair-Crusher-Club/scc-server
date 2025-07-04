package club.staircrusher.place.application.port.`in`.accessibility.toilet_review

import club.staircrusher.place.application.port.`in`.accessibility.result.WithUserInfo
import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.result.toDomainModel
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService

@Component
class ListToiletReviewsUseCase(
    private val toiletReviewService: ToiletReviewService,
    private val placeApplicationService: PlaceApplicationService,
    private val userApplicationService: UserApplicationService,
    private val transactionManager: TransactionManager,
) {
    fun handle(placeId: String) = transactionManager.doInTransaction(isReadOnly = true) {
        val buildingId = placeApplicationService.findPlace(placeId)?.building?.id
        val toiletReviews = toiletReviewService.listByPlaceId(placeId) +
                (buildingId?.let { toiletReviewService.listByBuildingId(it) } ?: emptyList())
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
