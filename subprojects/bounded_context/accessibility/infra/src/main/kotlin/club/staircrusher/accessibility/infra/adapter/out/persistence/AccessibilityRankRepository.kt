package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityRankRepository
import club.staircrusher.accessibility.domain.model.AccessibilityRank
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class AccessibilityRankRepository(
    val db: DB,
): AccessibilityRankRepository {
    private val queries = db.accessibilityRankQueries

    override fun findTopNUsers(n: Int): List<AccessibilityRank> {
        return queries.findTopNUsers(n.toLong()).executeAsList().map {
            it.toDomainModel()
        }
    }

    override fun findByUserId(userId: String): AccessibilityRank? {
        return queries.findByUserId(userId).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByRank(rank: Long): AccessibilityRank? {
        return queries.findByRank(rank).executeAsOneOrNull()?.toDomainModel()
    }

    override fun save(entity: AccessibilityRank): AccessibilityRank {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<AccessibilityRank>) {
        entities.forEach { queries.save(it.toPersistenceModel()) }
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): AccessibilityRank {
        return queries.findById(id).executeAsOne().toDomainModel()
    }

    override fun findByIdOrNull(id: String): AccessibilityRank? {
        return queries.findById(id).executeAsOneOrNull()?.toDomainModel()
    }
}
