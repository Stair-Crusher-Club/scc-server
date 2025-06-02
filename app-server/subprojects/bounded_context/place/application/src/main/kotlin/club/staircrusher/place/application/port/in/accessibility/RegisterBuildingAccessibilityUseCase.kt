package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.place.application.port.`in`.accessibility.result.RegisterBuildingAccessibilityResult
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class RegisterBuildingAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val accessibilityApplicationService: AccessibilityApplicationService,
    private val challengeService: ChallengeService,
    private val accessibilityImagePipeline: AccessibilityImagePipeline,
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
        accessibilityImagePipeline.asyncPostProcessImages(
            registerResult.buildingAccessibility.newEntranceAccessibilityImages + registerResult.buildingAccessibility.newElevatorAccessibilityImages
        )
        val challengeContributions =
            challengeService.contributeToSatisfiedChallenges(
                userId = userId,
                contribution = ChallengeService.Contribution.BuildingAccessibility(
                    buildingAccessibilityId = registerResult.buildingAccessibility.id,
                    buildingAccessibilityAddress = ChallengeAddress(registerResult.building),
                )
            )
        return@doInTransaction RegisterBuildingAccessibilityUseCaseResult(
            registerBuildingAccessibilityResult = registerResult,
            challengeContributions = challengeContributions
        )
    }
}
