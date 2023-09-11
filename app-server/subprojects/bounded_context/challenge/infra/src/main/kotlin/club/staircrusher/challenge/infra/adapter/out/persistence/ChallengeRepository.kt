package club.staircrusher.challenge.infra.adapter.out.persistence

import club.staircrusher.challenge.application.port.out.persistence.ChallengeRepository
import club.staircrusher.challenge.domain.model.Challenge
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ChallengeRepository(
    val db: DB
) : ChallengeRepository {
    private val queries = db.challengeQueries

    override fun findById(id: String): Challenge {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("Challenge of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): Challenge? {
        return queries.findById(id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByIds(challengeIds: Collection<String>): List<Challenge> {
        return queries.findByIds(challengeIds)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun remove(id: String) {
        queries.removeById(id)
    }

    override fun save(entity: Challenge): Challenge {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<Challenge>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }
}
