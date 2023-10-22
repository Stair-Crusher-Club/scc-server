package club.staircrusher.challenge.infra.adapter.out.persistence

import club.staircrusher.challenge.application.port.out.persistence.ChallengeRankRepository
import club.staircrusher.challenge.domain.model.ChallengeRank
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.challenge.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ChallengeRankRepository(
    private val db: DB
): ChallengeRankRepository {
    private val queries = db.challengeRankQueries

    override fun findTopNUsers(challengeId: String, n: Int): List<ChallengeRank> {
        return queries.findTopNUsers(challengeId, n.toLong())
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByUserId(challengeId: String, userId: String): ChallengeRank? {
        return queries.findByUserId(challengeId, userId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByRank(challengeId: String, rank: Long): ChallengeRank? {
        return queries.findByRank(challengeId, rank)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findNextRank(challengeId: String, rank: Long): ChallengeRank? {
        return queries.findNextRank(challengeId, rank)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findLastRank(challengeId: String): Long? {
        return queries.findLastRank(challengeId).executeAsOneOrNull()
    }

    override fun findByContributionCount(challengeId: String, contributionCount: Int): ChallengeRank? {
        return queries.findByContributionCount(challengeId, contributionCount)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findAll(challengeId: String): List<ChallengeRank> {
        return queries.findAll(challengeId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun save(entity: ChallengeRank): ChallengeRank {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<ChallengeRank>) {
        entities.forEach { save(it) }
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun removeAll(challengeId: String) {
        queries.removeAllByChallengeId(challengeId)
    }

    override fun findById(id: String): ChallengeRank {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("ChallengeRank of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): ChallengeRank? {
        return queries.findById(id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }
}
