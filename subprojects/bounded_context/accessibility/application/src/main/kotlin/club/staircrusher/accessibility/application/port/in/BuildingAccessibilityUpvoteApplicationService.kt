package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityUpvote
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import java.time.Clock

@Component
class BuildingAccessibilityUpvoteApplicationService(
    private val transactionManager: TransactionManager,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val buildingAccessibilityUpvoteRepository: BuildingAccessibilityUpvoteRepository,
    private val clock: Clock,
) {
    fun giveUpvote(
        user: AuthUser,
        buildingAccessibilityId: String,
    ) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val buildingAccessibility = buildingAccessibilityRepository.findById(buildingAccessibilityId)
        val existingUpvote = buildingAccessibilityUpvoteRepository.findByUserAndBuildingAccessibilityAndNotDeleted(
            user.id,
            buildingAccessibility,
        )

        existingUpvote ?: buildingAccessibilityUpvoteRepository.save(
            BuildingAccessibilityUpvote(
                id = EntityIdGenerator.generateRandom(),
                userId = user.id,
                buildingAccessibility = buildingAccessibility,
                createdAt = clock.instant(),
            )
        )
    }

    fun cancelUpvote(
        user: AuthUser,
        buildingAccessibilityId: String,
    ) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val buildingAccessibility = buildingAccessibilityRepository.findById(buildingAccessibilityId)
        val upvote = buildingAccessibilityUpvoteRepository.findByUserAndBuildingAccessibilityAndNotDeleted(
            user.id,
            buildingAccessibility,
        ) ?: return@doInTransaction

        upvote.cancel(clock.instant())
        buildingAccessibilityUpvoteRepository.save(upvote)
    }
}
