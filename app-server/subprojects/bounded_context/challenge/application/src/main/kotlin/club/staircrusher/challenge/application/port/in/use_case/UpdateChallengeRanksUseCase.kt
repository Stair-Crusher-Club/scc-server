package club.staircrusher.challenge.application.port.`in`.use_case

import club.staircrusher.challenge.application.port.out.persistence.ChallengeContributionRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRankRepository
import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionIsolationLevel
import club.staircrusher.stdlib.persistence.TransactionManager
import mu.KotlinLogging
import java.time.Duration

@Component
class UpdateChallengeRanksUseCase(
    private val transactionManager: TransactionManager,
    private val challengeRepository: ChallengeRepository,
    private val challengeRankRepository: ChallengeRankRepository,
    private val challengeContributionRepository: ChallengeContributionRepository,
    private val challengeParticipationRepository: ChallengeParticipationRepository,
) {
    private val logger = KotlinLogging.logger {}

    fun handle() {
        logger.info { "Starting to update challenge ranks" }

        val challenges = transactionManager.doInTransaction(isReadOnly = true) {
            val endsAtAfter = SccClock.instant() - Duration.ofDays(1)
            challengeRepository.findAllByEndsAtIsNullOrEndsAtAfterOrderByCreatedAtDesc(endsAtAfter)
        }

        logger.info { "Found ${challenges.size} challenges to update ranks" }

        challenges.forEach { challenge ->
            val now = SccClock.instant()
            transactionManager.doInTransaction(TransactionIsolationLevel.SERIALIZABLE) {
                val contributions = challengeContributionRepository.findByChallengeId(challenge.id)
                val participants = challengeParticipationRepository.findByChallengeId(challenge.id)

                val userIds = participants.map { it.userId }.toSet()
                val contributionUserIds = contributions.map { it.userId }.toSet()
                val missingUserIds = userIds - contributionUserIds
                val userWithNoContribution = missingUserIds.map { userId -> userId to emptyList<ChallengeContribution>() }

                val ranks = (contributions.groupBy { it.userId } + userWithNoContribution)
                    .map { (userId, contributions) ->
                        ChallengeRank(
                            id = EntityIdGenerator.generateRandom(),
                            challengeId = challenge.id,
                            userId = userId,
                            contributionCount = contributions.size.toLong(),
                            rank = -1,
                            createdAt = now,
                            updatedAt = now,
                        )
                    }

                var nextRank = 1L
                val updatedRanks = ranks.groupBy { it.contributionCount }
                    .toSortedMap(compareByDescending { it })
                    .flatMap { (_, ranks) ->
                        val currentRank = nextRank
                        nextRank += ranks.size
                        ranks.map {
                            it.apply { rank = currentRank; updatedAt = now }
                        }
                    }
                challengeRankRepository.deleteByChallengeId(challenge.id)
                challengeRankRepository.saveAll(updatedRanks)
            }
        }

        logger.info { "Finished updating challenge ranks" }
    }
}
