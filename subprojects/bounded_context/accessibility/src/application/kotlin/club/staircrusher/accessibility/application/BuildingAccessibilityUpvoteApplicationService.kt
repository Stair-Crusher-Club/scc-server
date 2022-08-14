package club.staircrusher.accessibility.application

import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityRepository
import club.staircrusher.accessibility.domain.service.BuildingAccessibilityUpvoteService
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager

class BuildingAccessibilityUpvoteApplicationService(
    private val transactionManager: TransactionManager,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingAccessibilityUpvoteService: BuildingAccessibilityUpvoteService,
) {
    fun giveUpvote(authUser: AuthUser, buildingAccessibilityId: String) = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val buildingAccessibility = buildingAccessibilityRepository.findById(buildingAccessibilityId)
        buildingAccessibilityUpvoteService.giveUpvote(authUser, buildingAccessibility)
    }

    fun cancelUpvote(authUser: AuthUser, buildingAccessibilityId: String) = transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
        val buildingAccessibility = buildingAccessibilityRepository.findById(buildingAccessibilityId)
        buildingAccessibilityUpvoteService.cancelUpvote(authUser, buildingAccessibility)
    }
}
