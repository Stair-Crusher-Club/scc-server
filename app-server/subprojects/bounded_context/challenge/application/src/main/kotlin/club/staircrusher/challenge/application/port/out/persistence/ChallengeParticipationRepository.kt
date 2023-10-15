package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface ChallengeParticipationRepository : EntityRepository<ChallengeParticipation, String> {
    fun findByUserId(userId: String): List<ChallengeParticipation>
    fun findByChallengeId(challengeId: String): List<ChallengeParticipation>
    fun findByChallengeIdAndUserId(challengeId: String, userId: String): List<ChallengeParticipation>
    fun challengeCountByUserId(userId: String): Long
    fun userCountByChallengeId(challengeId: String): Long
    fun remove(userId: String, challengeId: String)
}
