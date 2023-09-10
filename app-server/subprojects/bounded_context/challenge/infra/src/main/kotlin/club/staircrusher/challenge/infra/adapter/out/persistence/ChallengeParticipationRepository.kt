package club.staircrusher.challenge.infra.adapter.out.persistence

import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ChallengeParticipationRepository(
    val db: DB
) : ChallengeParticipationRepository {
    private val queries = db.challengeParticipationQueries

    override fun findByUserId(userId: String): List<ChallengeParticipation> {
        TODO("Not yet implemented")
    }

    override fun findByChallengeId(challengeId: String): List<ChallengeParticipation> {
        TODO("Not yet implemented")
    }

    override fun countByUserId(userId: String): Int {
        TODO("Not yet implemented")
    }

    override fun countByChallengeId(challengeId: String): Int {
        TODO("Not yet implemented")
    }

    override fun remove(userId: String, challengeId: String) {
        TODO("Not yet implemented")
    }

    override fun save(entity: ChallengeParticipation): ChallengeParticipation {
        TODO("Not yet implemented")
    }

    override fun saveAll(entities: Collection<ChallengeParticipation>) {
        TODO("Not yet implemented")
    }

    override fun removeAll() {
        TODO("Not yet implemented")
    }

    override fun findById(id: String): ChallengeParticipation {
        TODO("Not yet implemented")
    }

    override fun findByIdOrNull(id: String): ChallengeParticipation? {
        TODO("Not yet implemented")
    }
}
