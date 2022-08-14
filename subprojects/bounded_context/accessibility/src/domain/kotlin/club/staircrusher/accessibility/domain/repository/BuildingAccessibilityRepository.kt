package club.staircrusher.accessibility.domain.repository

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.stdlib.geography.EupMyeonDong

interface BuildingAccessibilityRepository : EntityRepository<BuildingAccessibility, String> {
    fun findByBuildingIds(buildingIds: Collection<String>): List<BuildingAccessibility>
    fun findByBuildingId(buildingId: String): BuildingAccessibility?
    fun findByPlaceIds(placeIds: Collection<String>): List<BuildingAccessibility>
    fun findByEupMyeonDong(eupMyeonDong: EupMyeonDong): List<BuildingAccessibility>
    fun countByUserId(userId: String): Int
}
