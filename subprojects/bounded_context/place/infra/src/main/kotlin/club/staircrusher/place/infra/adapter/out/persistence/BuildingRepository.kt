package club.staircrusher.place.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.place.application.port.out.persistence.BuildingRepository
import club.staircrusher.place.domain.model.Building
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.EupMyeonDong

@Component
class BuildingRepository(db: DB): BuildingRepository{
    private val buildingQueries = db.buildingQueries

    override fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int {
        return buildingQueries.countByEupMyeonDong(
            id = eupMyeonDong.id,
            name = eupMyeonDong.name,
            eupMyeonDongId = eupMyeonDong.id,
        ).executeAsOne().toInt()
    }

    override fun findByIdIn(ids: Collection<String>): List<Building> {
        return buildingQueries.findByIdIn(ids).executeAsList().map { it.toDomainModel() }
    }

    override fun save(entity: Building): Building {
        buildingQueries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entity: Collection<Building>): Building {
        entity.forEach {
            buildingQueries.save(it.toPersistenceModel())
        }

        return entity.first()
    }

    override fun removeAll() {
        buildingQueries.removeAll()
    }

    override fun findById(id: String): Building {
        return buildingQueries.findById(id).executeAsOne().toDomainModel()
    }

    override fun findByIdOrNull(id: String): Building? {
        return buildingQueries.findById(id).executeAsOneOrNull()?.toDomainModel()
    }
}
