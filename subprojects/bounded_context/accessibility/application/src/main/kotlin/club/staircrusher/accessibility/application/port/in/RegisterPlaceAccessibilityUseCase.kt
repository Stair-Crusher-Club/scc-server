package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.result.GetAccessibilityResult
import club.staircrusher.accessibility.application.port.`in`.result.RegisterPlaceAccessibilityResult
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class RegisterPlaceAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    fun handle(
        userId: String?,
        createPlaceAccessibilityParams: PlaceAccessibilityRepository.CreateParams,
        createPlaceAccessibilityCommentParams: PlaceAccessibilityCommentRepository.CreateParams?,
    ) : Pair<RegisterPlaceAccessibilityResult, GetAccessibilityResult> = transactionManager.doInTransaction {
        val registerResult = accessibilityApplicationService.doRegisterPlaceAccessibility(createPlaceAccessibilityParams, createPlaceAccessibilityCommentParams)
        val getAccessibilityResult = accessibilityApplicationService.doGetAccessibility(createPlaceAccessibilityParams.placeId, userId)
        Pair(registerResult, getAccessibilityResult)
    }
}
