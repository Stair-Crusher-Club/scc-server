package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRankRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager

/**
 * Update the users' ranks of all challenges.
 */
@Component
class UpdateChallengeRanksUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
    private val challengeRankRepository: ChallengeRankRepository,
    private val challengeContributionRepository: ChallengeContributionRepository,
) {
    fun handle() = transactionManager.doInTransaction {
        val challenges = challengeRepository.findAllOrderByCreatedAtDesc()
        challenges.forEach { challenge ->
            val now = SccClock.instant()
            val lastRank = challengeRankRepository.findByContributionCount(challenge.id, 0)?.rank ?: 1
            val contributions = challengeContributionRepository.findByChallengeId(challenge.id)

            val ranks = contributions.groupBy { it.userId }
                .map { (userId, contributions) ->
                    val contributionCount = contributions.size
                    val challengeRank = challengeRankRepository.findByUserId(challenge.id, userId) ?: ChallengeRank(
                        id = EntityIdGenerator.generateRandom(),
                        challengeId = challenge.id,
                        userId = userId,
                        contributionCount = contributionCount,
                        rank = lastRank,
                        createdAt = now,
                        updatedAt = now,
                    )

                    challengeRank.copy(
                        contributionCount = contributionCount,
                        updatedAt = now,
                    )
                }

            var nextRank = 1L
            val updatedRanks = ranks.groupBy { it.contributionCount }
                .toSortedMap(compareByDescending { it })
                .flatMap { (_, ranks) ->
                    val currentRank = nextRank
                    nextRank += ranks.size
                    ranks.map { it.copy(rank = currentRank, updatedAt = now) }
                }
            challengeRankRepository.saveAll(updatedRanks)
        }
    }
}
