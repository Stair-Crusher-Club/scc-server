package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.`in`.result.RegisterBuildingAccessibilityResult
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class RegisterBuildingAccessibilityUseCase(
    private val transactionManager: TransactionManager,
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    fun handle(
        createBuildingAccessibilityParams: BuildingAccessibilityRepository.CreateParams,
        createBuildingAccessibilityCommentParams: BuildingAccessibilityCommentRepository.CreateParams?,
    ) : RegisterBuildingAccessibilityResult = transactionManager.doInTransaction {
        accessibilityApplicationService.doRegisterBuildingAccessibility(createBuildingAccessibilityParams, createBuildingAccessibilityCommentParams)
    }
}
