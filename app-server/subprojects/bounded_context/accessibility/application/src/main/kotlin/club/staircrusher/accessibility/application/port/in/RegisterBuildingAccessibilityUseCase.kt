package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.result.RegisterBuildingAccessibilityResult
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class RegisterBuildingAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val accessibilityApplicationService: AccessibilityApplicationService,
    private val challengeService: ChallengeService
) {
    data class RegisterBuildingAccessibilityUseCaseResult(
        val registerBuildingAccessibilityResult: RegisterBuildingAccessibilityResult,
        val challengeContributions: List<ChallengeContribution>
    )

    fun handle(
        userId: String,
        createBuildingAccessibilityParams: BuildingAccessibilityRepository.CreateParams,
        createBuildingAccessibilityCommentParams: BuildingAccessibilityCommentRepository.CreateParams?,
    ): RegisterBuildingAccessibilityUseCaseResult = transactionManager.doInTransaction {
        val registerResult = accessibilityApplicationService.doRegisterBuildingAccessibility(
            createBuildingAccessibilityParams,
            createBuildingAccessibilityCommentParams
        )
        val challengeContributions =
            challengeService.contributeToSatisfiedChallenges(
                userId = userId,
                contribution = ChallengeService.Contribution.BuildingAccessibility(
                    buildingAccessibilityId = registerResult.buildingAccessibility.id,
                    buildingAccessibilityAddress = registerResult.building.address.let {
                        ChallengeAddress(
                            siDo = it.siDo,
                            siGunGu = it.siGunGu,
                            eupMyeonDong = it.eupMyeonDong,
                            li = it.li,
                            roadName = it.roadName
                        )
                    }
                )
            )
        return@doInTransaction RegisterBuildingAccessibilityUseCaseResult(
            registerBuildingAccessibilityResult = registerResult,
            challengeContributions = challengeContributions
        )
    }
}
