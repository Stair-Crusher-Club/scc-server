package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.domain_event.BuildingAccessibilityCommentDeletedEvent
import club.staircrusher.place.application.port.`in`.place.toBuilding
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class HandleBuildingAccessibilityCommentDeletedEventUseCase(
    private val transactionManager: TransactionManager,
    private val challengeService: ChallengeService,
) {
    fun handle(event: BuildingAccessibilityCommentDeletedEvent) = transactionManager.doInTransaction {
        if (event.commentRegisterer.id == null) {
            return@doInTransaction
        }
        challengeService.deleteContributions(
            userId = event.commentRegisterer.id!!,
            contribution = ChallengeService.Contribution.BuildingAccessibility(
                buildingAccessibilityId = event.id,
                buildingAccessibilityAddress = ChallengeAddress(event.building.toBuilding()),
            ),
        )
    }
}
