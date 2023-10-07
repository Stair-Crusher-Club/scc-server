package club.staircrusher.challenge.infra.adapter.out.persistence

import club.staircrusher.challenge.application.port.out.persistence.ChallengeParticipationRepository
import club.staircrusher.challenge.domain.model.ChallengeParticipation
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Suppress("TooManyFunctions")
@Component
class ChallengeParticipationRepository(
    val db: DB
) : ChallengeParticipationRepository {
    private val queries = db.challengeParticipationQueries

    override fun findById(id: String): ChallengeParticipation {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("ChallengeParticipation of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): ChallengeParticipation? {
        return queries.findById(id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByUserId(userId: String): List<ChallengeParticipation> {
        return queries.findByUserId(userId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByChallengeId(challengeId: String): List<ChallengeParticipation> {
        return queries.findByChallengeId(challengeId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByChallengeIdAndUserId(challengeId: String, userId: String): ChallengeParticipation? {
        return queries.findByUserIdAndChallengeId(challengeId, userId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun userCountByChallengeId(challengeId: String): Long {
        return queries.userCountByChallengeId(challengeId).executeAsOne()
    }

    override fun challengeCountByUserId(userId: String): Long {
        return queries.challengeCountByUserId(userId).executeAsOne()
    }

    override fun remove(userId: String, challengeId: String) {
        queries.removeById(userId, challengeId)
    }

    override fun save(entity: ChallengeParticipation): ChallengeParticipation {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<ChallengeParticipation>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }
}
