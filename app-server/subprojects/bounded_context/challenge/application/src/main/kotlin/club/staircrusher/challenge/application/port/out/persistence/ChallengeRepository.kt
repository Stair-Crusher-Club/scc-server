package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ChallengeRepository : EntityRepository<Challenge, String> {
    fun findByChallengeId(challengeId: String): Challenge?
    fun findByChallengeIds(challengeIds: Collection<String>): List<Challenge>
    fun countAll(): Int
    fun remove(id: String)
}
