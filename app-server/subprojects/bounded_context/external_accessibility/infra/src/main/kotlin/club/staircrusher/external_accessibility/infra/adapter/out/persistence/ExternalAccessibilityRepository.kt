package club.staircrusher.external_accessibility.infra.adapter.out.persistence

import club.staircrusher.external_accessibility.application.port.out.persistence.ExternalAccessibilityRepository
import club.staircrusher.external_accessibility.domain.model.ExternalAccessibility
import club.staircrusher.external_accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.external_accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.external_accessibility.ExternalAccessibilityCategory

@Suppress("TooManyFunctions")
@Component
class ExternalAccessibilityRepository(
    val db: DB
) : ExternalAccessibilityRepository {
    private val queries = db.externalAccessibilityQueries

    override fun findById(id: String): ExternalAccessibility {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("ExternalAccessibility of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): ExternalAccessibility? {
        return queries.findById(id)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun saveAll(entities: Collection<ExternalAccessibility>) {
        entities.forEach { save(it) }
    }

    override fun save(entity: ExternalAccessibility): ExternalAccessibility {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun findAll(): List<ExternalAccessibility> {
        return queries.findAll()
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByCategoryIn(categories: Set<ExternalAccessibilityCategory>): List<ExternalAccessibility> {
        return queries.findByCategoryIn(categories)
            .executeAsList()
            .map { it.toDomainModel() }
    }
}
