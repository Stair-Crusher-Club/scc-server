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
        if (ids.isEmpty()) {
            // empty list로 쿼리를 할 경우 sqldelight가 제대로 처리하지 못하는 문제가 있다.
            // select * from entity where entity.id in (); <- 이런 식으로 쿼리를 날리는데, () 부분이 syntax error이다.
            // 따라서 ids가 empty면 early return을 해준다.
            return emptyList()
        }
        return buildingQueries.findByIdIn(ids).executeAsList().map { it.toDomainModel() }
    }

    override fun save(entity: Building): Building {
        buildingQueries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<Building>) {
        entities.forEach(::save)
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
