package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.stdlib.geography.EupMyeonDong
import club.staircrusher.place.domain.model.Place

interface PlaceRepository : EntityRepository<Place, String> {
    fun findByNameContains(searchTextRegex: String): List<Place>
    /**
     * fetch join:
     * - building
     */
    fun findByBuildingId(buildingId: String): List<Place>
    /**
     * fetch join:
     * - building
     */
    fun findByIdIn(ids: Collection<String>): List<Place>
    fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int
}
