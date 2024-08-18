package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityUpvoteRepository
import club.staircrusher.accessibility.domain.model.PlaceAccessibilityUpvote
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.repository.findByIdOrNull
import java.time.Clock

@Component
class GivePlaceAccessibilityUpvoteUseCase(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val placeAccessibilityUpvoteRepository: PlaceAccessibilityUpvoteRepository,
    private val clock: Clock,
) {
    fun handle(
        user: AuthUser,
        placeAccessibilityId: String,
    ) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        placeAccessibilityRepository.findByIdOrNull(placeAccessibilityId)
            ?: throw IllegalArgumentException("PlaceAccessibility of id $placeAccessibilityId does not exist.")
        val existingUpvote = placeAccessibilityUpvoteRepository.findExistingUpvote(user.id, placeAccessibilityId)
        existingUpvote ?: placeAccessibilityUpvoteRepository.save(
            PlaceAccessibilityUpvote(
                id = EntityIdGenerator.generateRandom(),
                userId = user.id,
                placeAccessibilityId = placeAccessibilityId,
                createdAt = clock.instant(),
            )
        )
    }
}
