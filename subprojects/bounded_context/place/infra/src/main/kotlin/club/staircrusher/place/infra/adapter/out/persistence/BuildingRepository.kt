package club.staircrusher.place.infra.adapter.out.persistence

import club.staircrusher.place.application.port.out.persistence.BuildingRepository
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.infra.PlaceDatabase
import club.staircrusher.place.infra.toBuilding
import club.staircrusher.stdlib.geography.EupMyeonDong

// @Component
class BuildingRepository(placeDatabase: PlaceDatabase): BuildingRepository{
    private val buildingQueries = placeDatabase.buildingQueries

    override fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int {
        return buildingQueries.countByEupMyeonDong(
            id = eupMyeonDong.id,
            name = eupMyeonDong.name,
            siGunGuId = eupMyeonDong.siGunGu.id,
        ).executeAsOne().toInt()
    }

    override fun findByIdIn(ids: Collection<String>): List<Building> {
        return buildingQueries.findByIdIn(ids).executeAsList().map { it.toBuilding() }
    }

    override fun save(entity: Building): Building {
        buildingQueries.save(entity.toBuilding())
        return entity
    }

    override fun saveAll(entity: Collection<Building>): Building {
        entity.forEach {
            buildingQueries.save(it.toBuilding())
        }

        return entity.first()
    }

    override fun removeAll() {
        buildingQueries.removeAll()
    }

    override fun findById(id: String): Building {
        return buildingQueries.findById(id).executeAsOne().toBuilding()
    }

    override fun findByIdOrNull(id: String): Building? {
        return buildingQueries.findById(id).executeAsOneOrNull()?.toBuilding()
    }
}