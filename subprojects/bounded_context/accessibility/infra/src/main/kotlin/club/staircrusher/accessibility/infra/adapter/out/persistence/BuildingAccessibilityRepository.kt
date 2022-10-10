package club.staircrusher.accessibility.infra.adapter.out.persistence

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.repository.BuildingAccessibilityRepository
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toDomainModel
import club.staircrusher.accessibility.infra.adapter.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.EupMyeonDong

@Component
class BuildingAccessibilityRepository(
    db: DB,
) : BuildingAccessibilityRepository {
    private val queries = db.buildingAccessibilityQueries

    override fun save(entity: BuildingAccessibility): BuildingAccessibility {
        queries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entity: Collection<BuildingAccessibility>): BuildingAccessibility {
        entity.forEach(::save)
        return entity.first()
    }

    override fun removeAll() {
        queries.removeAll()
    }

    override fun findById(id: String): BuildingAccessibility {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("BuildingAccessibility of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): BuildingAccessibility? {
        return queries.findById(id = id).executeAsOneOrNull()?.toDomainModel()
    }

    override fun findByBuildingIds(buildingIds: Collection<String>): List<BuildingAccessibility> {
        return queries.findByBuildingIds(buildingIds = buildingIds)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByBuildingId(buildingId: String): BuildingAccessibility? {
        return queries.findByBuildingId(buildingId = buildingId)
            .executeAsOneOrNull()
            ?.toDomainModel()
    }

    override fun findByPlaceIds(placeIds: Collection<String>): List<BuildingAccessibility> {
        return queries.findByPlaceIds(placeIds = placeIds)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun findByEupMyeonDong(eupMyeonDong: EupMyeonDong): List<BuildingAccessibility> {
        return queries.findByEupMyeonDong(eupMyeonDongId = eupMyeonDong.id)
            .executeAsList()
            .map { it.toDomainModel() }
    }

    override fun countByUserId(userId: String): Int {
        return queries.countByUserId(userId = userId).executeAsOne().toInt()
    }
}
