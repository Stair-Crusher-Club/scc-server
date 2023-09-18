package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.ChallengeContribution
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ChallengeContributionRepository : EntityRepository<ChallengeContribution, String> {
    fun findByUserId(userId: String): List<ChallengeContribution>
    fun findByUserIds(userIds: List<String>): List<ChallengeContribution>
    fun findByChallengeId(challengeId: String): List<ChallengeContribution>
    fun findByChallengeIds(challengeIds: List<String>): List<ChallengeContribution>
    fun countByChallengeId(challengeId: String): Long
    fun remove(contributionId: String)
}
