package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ChallengeRepository : EntityRepository<Challenge, String> {
    fun findByIds(challengeIds: Collection<String>): List<Challenge>
    fun remove(id: String)
}
