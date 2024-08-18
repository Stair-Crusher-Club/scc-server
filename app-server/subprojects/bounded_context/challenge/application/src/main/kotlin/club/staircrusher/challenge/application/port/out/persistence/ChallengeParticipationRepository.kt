package club.staircrusher.challenge.application.port.out.persistence

import club.staircrusher.challenge.domain.model.ChallengeParticipation
import org.springframework.data.repository.CrudRepository

interface ChallengeParticipationRepository : CrudRepository<ChallengeParticipation, String> {
    fun findByUserId(userId: String): List<ChallengeParticipation>
    fun findByChallengeId(challengeId: String): List<ChallengeParticipation>
    fun findByChallengeIdAndUserId(challengeId: String, userId: String): List<ChallengeParticipation>
    fun countByChallengeId(challengeId: String): Long
}
