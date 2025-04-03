package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityRepository
import club.staircrusher.place.application.port.out.accessibility.persistence.BuildingAccessibilityUpvoteRepository
import club.staircrusher.place.domain.model.accessibility.BuildingAccessibilityUpvote
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull
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
        val existingUpvote = buildingAccessibilityUpvoteRepository.findExistingUpvote(
            user.id,
            buildingAccessibilityId,
        )
        existingUpvote ?: buildingAccessibilityRepository.findByIdOrNull(buildingAccessibilityId)?.let {
            buildingAccessibilityUpvoteRepository.save(
                BuildingAccessibilityUpvote(
                    id = EntityIdGenerator.generateRandom(),
                    userId = user.id,
                    buildingAccessibilityId = it.id,
                    createdAt = clock.instant(),
                )
            )
        }
    }

    fun cancelUpvote(
        user: AuthUser,
        buildingAccessibilityId: String,
    ) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val upvote = buildingAccessibilityUpvoteRepository.findExistingUpvote(
            user.id,
            buildingAccessibilityId,
        ) ?: return@doInTransaction
        upvote.cancel(clock.instant())
        buildingAccessibilityUpvoteRepository.save(upvote)
    }
}
