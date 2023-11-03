package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.domain_event.PlaceAccessibilityDeletedEvent
import club.staircrusher.place.application.port.`in`.toPlace
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class HandlePlaceAccessibilityDeletedEventUseCase(
    private val transactionManager: TransactionManager,
    private val challengeService: ChallengeService,
) {
    fun handle(event: PlaceAccessibilityDeletedEvent) = transactionManager.doInTransaction {
        if (event.accessibilityRegisterer.id == null) {
            return@doInTransaction
        }
        challengeService.deleteContributions(
            userId = event.accessibilityRegisterer.id!!,
            contribution = ChallengeService.Contribution.PlaceAccessibility(
                placeAccessibilityId = event.id,
                placeAccessibilityAddress = ChallengeAddress(event.place.toPlace()),
            ),
        )
    }
}
