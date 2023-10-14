package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ChallengeRankRepository : EntityRepository<ChallengeRank, String> {
    fun findTopNUsers(challenge: Challenge, n: Int): List<ChallengeRank>
    fun findByUserId(challenge: Challenge, userId: String): ChallengeRank?
    fun findByRank(challenge: Challenge, rank: Long): ChallengeRank?
    fun findNextRank(challenge: Challenge, rank: Long): ChallengeRank?
    fun findByContributionCount(challenge: Challenge, contributionCount: Int): ChallengeRank?
    fun findAll(challenge: Challenge): List<ChallengeRank>
}
