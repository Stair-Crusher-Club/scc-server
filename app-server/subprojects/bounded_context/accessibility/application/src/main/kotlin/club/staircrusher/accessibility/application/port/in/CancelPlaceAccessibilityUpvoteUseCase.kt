package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityUpvoteRepository
import club.staircrusher.stdlib.auth.AuthUser
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import java.time.Clock

@Component
class CancelPlaceAccessibilityUpvoteUseCase(
    private val transactionManager: TransactionManager,
    private val placeAccessibilityUpvoteRepository: PlaceAccessibilityUpvoteRepository,
    private val clock: Clock,
) {
    fun handle(
        user: AuthUser,
        placeAccessibilityId: String,
    ) = transactionManager.doInTransaction(TransactionIsolationLevel.REPEATABLE_READ) {
        val upvote =
            placeAccessibilityUpvoteRepository.findExistingUpvote(user.id, placeAccessibilityId) ?: return@doInTransaction
        upvote.cancel(clock.instant())
        placeAccessibilityUpvoteRepository.save(upvote)
    }
}
