package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.domain_event.PlaceAccessibilityCommentDeletedEvent
import club.staircrusher.place.application.port.`in`.place.toPlace
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
                placeAccessibilityAddress = ChallengeAddress(event.place.toPlace()),
            ),
        )
    }
}
