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
        return placeQueries.findById(id).executeAsOne().toDomainModel()
    }

    override fun findByIdOrNull(id: String): Place? {
        return placeQueries.findById(id).executeAsOneOrNull()?.toDomainModel()
    }
}
