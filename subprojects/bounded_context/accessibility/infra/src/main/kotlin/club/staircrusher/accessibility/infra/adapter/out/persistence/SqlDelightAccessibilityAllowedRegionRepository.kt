package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.AccessibilityAllowedRegionRepository
import club.staircrusher.accessibility.domain.model.AccessibilityAllowedRegion
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class SqlDelightAccessibilityAllowedRegionRepository(
    db: DB
) : AccessibilityAllowedRegionRepository {
    private val queries = db.accessibilityAllowedRegionQueries

    override fun findAll(): List<AccessibilityAllowedRegion> {
        return queries.findAll().executeAsList().map { it.toDomainModel() }
    }

    override fun save(entity: AccessibilityAllowedRegion): AccessibilityAllowedRegion {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<AccessibilityAllowedRegion>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): AccessibilityAllowedRegion {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("AccessibilityAllowedRegion of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): AccessibilityAllowedRegion? {
        return queries.findById(id).executeAsOneOrNull()?.toDomainModel()
    }
}
