package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.place.domain.model.Building
import org.springframework.data.repository.CrudRepository

interface BuildingRepository : CrudRepository<Building, String>
