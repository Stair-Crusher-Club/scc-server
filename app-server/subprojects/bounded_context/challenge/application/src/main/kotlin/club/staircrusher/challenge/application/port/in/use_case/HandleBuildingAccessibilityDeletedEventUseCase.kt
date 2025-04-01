package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.domain_event.BuildingAccessibilityDeletedEvent
import club.staircrusher.place.application.port.`in`.place.toBuilding
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class HandleBuildingAccessibilityDeletedEventUseCase(
    private val transactionManager: TransactionManager,
    private val challengeService: ChallengeService,
) {
    fun handle(event: BuildingAccessibilityDeletedEvent) = transactionManager.doInTransaction {
        if (event.accessibilityRegisterer.id == null) {
            return@doInTransaction
        }
        challengeService.deleteContributions(
            userId = event.accessibilityRegisterer.id!!,
            contribution = ChallengeService.Contribution.BuildingAccessibilityComment(
                buildingAccessibilityCommentId = event.id,
                buildingAccessibilityAddress = ChallengeAddress(event.building.toBuilding()),
            ),
        )
    }
}
