package club.staircrusher.place.infra.adapter.out.persistence

import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.geography.EupMyeonDong
import org.springframework.stereotype.Component

@Component
class NoopPlaceRepository : PlaceRepository {
    override fun findByNameContains(searchTextRegex: String): List<Place> {
        return emptyList()
    }

    override fun findByBuildingId(buildingId: String): List<Place> {
        return emptyList()
    }

    override fun findByIdIn(ids: Collection<String>): List<Place> {
        return emptyList()
    }

    override fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int {
        return 0
    }

    override fun save(entity: Place): Place {
        return entity
    }

    override fun saveAll(entity: Collection<Place>): Place {
        return entity.first()
    }

    override fun removeAll() {
        // No-op
    }

    override fun findById(id: String): Place {
        throw IllegalArgumentException("Place of id $id does not exists")
    }

    override fun findByIdOrNull(id: String): Place? {
        return null
    }
}