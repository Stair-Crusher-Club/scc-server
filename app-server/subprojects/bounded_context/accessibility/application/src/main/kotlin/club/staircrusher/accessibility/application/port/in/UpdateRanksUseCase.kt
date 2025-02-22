package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityRankRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.domain.model.UserProfile

@Component
class UpdateRanksUseCase(
    private val accessibilityRankRepository: AccessibilityRankRepository,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val userApplicationService: UserApplicationService,
    private val transactionManager: TransactionManager,
) {
    /**
     * Get all users and update their rank
     */
    fun handle() {
        // update accessibility rank with count first
        transactionManager.doInTransaction {
            val userProfiles: List<UserProfile> = userApplicationService.getAllUserProfiles()
            val lastRank = accessibilityRankRepository.findRankByConqueredCount(0) ?: 1
            val now = SccClock.instant()

            val ranks = userProfiles.map {
                val conqueredCount = placeAccessibilityRepository.countByUserIdAndDeletedAtIsNull(it.userId)
                val accessibilityRank = accessibilityRankRepository.findFirstByUserId(it.userId) ?: AccessibilityRank(
                    id = EntityIdGenerator.generateRandom(),
                    userId = it.userId,
                    conqueredCount = conqueredCount,
                    rank = lastRank,
                    createdAt = now,
                    updatedAt = now,
                )
                accessibilityRank.updateConqueredCount(conqueredCount)

                accessibilityRankRepository.save(accessibilityRank)
            }

            var previousRank = 0L
            var currentRank = 1L
            var currentConqueredCount = -1

            val countPerConqueredCount = ranks
                .groupBy { it.conqueredCount }
                .toSortedMap(compareByDescending { it })

            countPerConqueredCount.forEach { (conqueredCount, ranks) ->
                if (conqueredCount != currentConqueredCount) {
                    previousRank = currentRank
                    ranks.forEach { it.updateRank(rank = currentRank) }
                } else {
                    // If the last conquest count in the previous batch is the same as the conquest
                    // count in the current batch, then they should have the same rank.
                    currentRank += ranks.size
                    ranks.forEach { it.updateRank(rank = previousRank) }
                }
                currentRank += ranks.size
                currentConqueredCount = conqueredCount
                accessibilityRankRepository.saveAll(ranks)
            }
        }
    }
}
