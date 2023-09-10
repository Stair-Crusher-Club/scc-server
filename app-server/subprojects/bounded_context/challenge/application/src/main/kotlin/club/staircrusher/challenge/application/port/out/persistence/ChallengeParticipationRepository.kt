package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ChallengeParticipationRepository : EntityRepository<ChallengeParticipation, String> {
    fun findByUserId(userId: String): List<ChallengeParticipation>
    fun findByChallengeId(challengeId: String): List<ChallengeParticipation>
    fun countByUserId(userId: String): Int
    fun countByChallengeId(challengeId: String): Int
    fun remove(userId: String, challengeId: String)
}
