package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityCommentRepository
import club.staircrusher.accessibility.domain.model.BuildingAccessibilityComment
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component

@Component
class BuildingAccessibilityCommentRepository(
    db: DB,
) : BuildingAccessibilityCommentRepository {
    private val queries = db.buildingAccessibilityCommentQueries

    override fun save(entity: BuildingAccessibilityComment): BuildingAccessibilityComment {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<BuildingAccessibilityComment>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): BuildingAccessibilityComment {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("BuildingAccessibilityComment of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): BuildingAccessibilityComment? {
        return queries.findById(id = id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByBuildingId(buildingId: String): List<BuildingAccessibilityComment> {
        return queries.findByBuildingId(buildingId = buildingId)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun removeByBuildingId(buildingId: String) {
        queries.removeByBuildingId(buildingId)
    }
}
