package club.staircrusher.place.infra.adapter.out.persistence

import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.place.application.port.out.persistence.BuildingRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.EupMyeonDong

@Component
class PlaceRepository(
    db: DB,
    private val buildingRepository: BuildingRepository,
) : PlaceRepository {
    private val placeQueries = db.placeQueries

    override fun findByNameContains(searchTextRegex: String): List<Place> {
        val places = placeQueries.findByNameContains("%$searchTextRegex%").executeAsList()
        val buildingIds = places.mapNotNull { it.building_id }
        val buildings = buildingRepository.findByIdIn(buildingIds).associateBy { it.id }

        return places.map { it.toDomainModel(buildings[it.building_id]!!) }
    }

    override fun findByBuildingId(buildingId: String): List<Place> {
        val places = placeQueries.findByBuildingId(buildingId).executeAsList()
        val building = buildingRepository.findById(buildingId)

        return places.map { it.toDomainModel(building) }
    }

    override fun findByIdIn(ids: Collection<String>): List<Place> {
        if (ids.isEmpty()) {
            // empty list로 쿼리를 할 경우 sqldelight가 제대로 처리하지 못하는 문제가 있다.
            // select * from entity where entity.id in (); <- 이런 식으로 쿼리를 날리는데, () 부분이 syntax error이다.
            // 따라서 ids가 empty면 early return을 해준다.
            return emptyList()
        }
        val places = placeQueries.findByIdIn(ids).executeAsList()
        val buildingIds = places.mapNotNull { it.building_id }
        val buildings = buildingRepository.findByIdIn(buildingIds).associateBy { it.id }

        return places.map { it.toDomainModel(buildings[it.building_id]!!) }
    }

    override fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int {
        return placeQueries.countByEupMyeonDong(
            id = eupMyeonDong.id,
            name = eupMyeonDong.name,
            eupMyeonDongId = eupMyeonDong.id,
        ).executeAsOne().toInt()
    }

    override fun save(entity: Place): Place {
        placeQueries.save(entity.toPersistenceModel())
        return entity
    }

    override fun saveAll(entities: Collection<Place>) {
        entities.forEach(::save)
    }

    override fun removeAll() {
        placeQueries.removeAll()
    }

    override fun findById(id: String): Place {
        return findByIdOrNull(id) ?: throw IllegalArgumentException("Place of id $id does not exist.")
    }

    override fun findByIdOrNull(id: String): Place? {
        return placeQueries.findById(id).executeAsOneOrNull()?.toDomainModel()
    }
}
