package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.domain_event.PlaceAccessibilityCommentDeletedEvent
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class HandlePlaceAccessibilityCommentDeletedEventUseCase(
    private val transactionManager: TransactionManager,
    private val challengeService: ChallengeService,
) {
    fun handle(event: PlaceAccessibilityCommentDeletedEvent) = transactionManager.doInTransaction {
        if (event.commentRegisterer.id == null) {
            return@doInTransaction
        }
        challengeService.deleteContributions(
            userId = event.commentRegisterer.id!!,
            contribution = ChallengeService.Contribution.PlaceAccessibilityComment(
                placeAccessibilityCommentId = event.id,
                placeAccessibilityAddress = ChallengeAddress(
                    siDo = event.place.address.siDo,
                    siGunGu = event.place.address.siGunGu,
                    eupMyeonDong = event.place.address.eupMyeonDong,
                    li = event.place.address.li,
                    roadName = event.place.address.roadName,
                ),
                placeCategoryValue = event.place.category?.name, // Place의 카테고리 이름 사용
            ),
        )
    }
}
