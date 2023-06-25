package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.stdlib.geography.EupMyeonDong
import club.staircrusher.place.domain.model.Building

interface BuildingRepository : EntityRepository<Building, String> {
    fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int
    fun findByIdIn(ids: Collection<String>): List<Building>
}
