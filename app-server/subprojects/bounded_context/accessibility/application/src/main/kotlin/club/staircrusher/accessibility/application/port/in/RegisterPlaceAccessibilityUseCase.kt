package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.result.GetAccessibilityResult
import club.staircrusher.accessibility.application.port.`in`.result.RegisterPlaceAccessibilityResult
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.challenge.application.port.`in`.ChallengeService
import club.staircrusher.challenge.domain.model.ChallengeAddress
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class RegisterPlaceAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val accessibilityApplicationService: AccessibilityApplicationService,
    private val challengeService: ChallengeService
) {
    data class RegisterPlaceAccessibilityUseCaseResult(
        val registerPlaceAccessibilityResult: RegisterPlaceAccessibilityResult,
        val getAccessibilityResult: GetAccessibilityResult,
        val challengeContributions: List<ChallengeContribution>
    )

    fun handle(
        userId: String?,
        createPlaceAccessibilityParams: PlaceAccessibilityRepository.CreateParams,
        createPlaceAccessibilityCommentParams: PlaceAccessibilityCommentRepository.CreateParams?,
    ): RegisterPlaceAccessibilityUseCaseResult = transactionManager.doInTransaction {
        val registerResult = accessibilityApplicationService.doRegisterPlaceAccessibility(
            createPlaceAccessibilityParams,
            createPlaceAccessibilityCommentParams
        )
        val getAccessibilityResult =
            accessibilityApplicationService.doGetAccessibility(createPlaceAccessibilityParams.placeId, userId)
        val challengeContributions = userId?.let { userId ->
            challengeService.contributeToSatisfiedChallenges(
                userId = userId,
                contribution = ChallengeService.Contribution.PlaceAccessibility(
                    placeAccessibilityId = registerResult.placeAccessibility.id,
                    placeAccessibilityAddress = registerResult.place.address.let {
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
        } ?: listOf()
        return@doInTransaction RegisterPlaceAccessibilityUseCaseResult(
            registerPlaceAccessibilityResult = registerResult,
            getAccessibilityResult = getAccessibilityResult,
            challengeContributions = challengeContributions
        )
    }
}
