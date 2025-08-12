package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.domain_event.BuildingAccessibilityCommentDeletedEvent
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
            contribution = ChallengeService.Contribution.BuildingAccessibilityComment(
                buildingAccessibilityCommentId = event.id,
                buildingAccessibilityAddress = ChallengeAddress(
                    siDo = event.building.address.siDo,
                    siGunGu = event.building.address.siGunGu,
                    eupMyeonDong = event.building.address.eupMyeonDong,
                    li = event.building.address.li,
                    roadName = event.building.address.roadName,
                ),
                placeCategoryValue = null, // BuildingAccessibilityComment는 카테고리가 없음
            ),
        )
    }
}
