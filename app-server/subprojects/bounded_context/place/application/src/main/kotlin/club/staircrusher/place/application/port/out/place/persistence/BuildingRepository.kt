package club.staircrusher.place.application.port.out.place.persistence

import club.staircrusher.place.domain.model.place.Building
import org.springframework.data.repository.CrudRepository

interface BuildingRepository : CrudRepository<Building, String>
