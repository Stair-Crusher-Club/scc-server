package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityRankRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.domain.model.User

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
            val users: List<User> = userApplicationService.getAllUsers()
            val lastRank = accessibilityRankRepository.findByConquestCount(0)?.rank ?: 1
            val now = SccClock.instant()

            val ranks = users.map {
                val conquestCount = placeAccessibilityRepository.countByUserId(it.id)
                val accessibilityRank = accessibilityRankRepository.findByUserId(it.id) ?: AccessibilityRank(
                    id = EntityIdGenerator.generateRandom(),
                    userId = it.id,
                    conquestCount = conquestCount,
                    rank = lastRank,
                    createdAt = now,
                    updatedAt = now,
                )

                accessibilityRankRepository.save(accessibilityRank.copy(
                    conquestCount = conquestCount,
                    updatedAt = now,
                ))
            }

            var previousRank = 0L
            var currentRank = 1L
            var currentConquestCount = -1

            val countPerConquestCount = ranks
                .groupBy { it.conquestCount }
                .toSortedMap(compareByDescending { it })

            countPerConquestCount.forEach { (conquestCount, ranks) ->
                val updatedRanks = if (conquestCount != currentConquestCount) {
                    previousRank = currentRank
                    ranks.map { it.copy(rank = currentRank, updatedAt = now) }
                } else {
                    // If the last conquest count in the previous batch is the same as the conquest
                    // count in the current batch, then they should have the same rank.
                    currentRank += ranks.size
                    ranks.map { it.copy(rank = previousRank, updatedAt = now) }
                }
                currentRank += ranks.size
                currentConquestCount = conquestCount
                accessibilityRankRepository.saveAll(updatedRanks)
            }
        }
    }
}
