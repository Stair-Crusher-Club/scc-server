package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.place.domain.model.Place
import org.springframework.data.repository.CrudRepository

interface PlaceRepository : CrudRepository<Place, String> {
    fun findByBuildingId(buildingId: String): List<Place>
}
