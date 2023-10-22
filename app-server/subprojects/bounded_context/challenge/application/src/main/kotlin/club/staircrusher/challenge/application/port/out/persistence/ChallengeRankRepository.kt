package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ChallengeRankRepository : EntityRepository<ChallengeRank, String> {
    fun findTopNUsers(challengeId: String, n: Int): List<ChallengeRank>
    fun findByUserId(challengeId: String, userId: String): ChallengeRank?
    fun findByRank(challengeId: String, rank: Long): ChallengeRank?
    fun findNextRank(challengeId: String, rank: Long): ChallengeRank?
    fun findByContributionCount(challengeId: String, contributionCount: Long): ChallengeRank?
    fun findLastRank(challengeId: String): Long?
    fun findAll(challengeId: String): List<ChallengeRank>
    fun removeAll(challengeId: String)
}
