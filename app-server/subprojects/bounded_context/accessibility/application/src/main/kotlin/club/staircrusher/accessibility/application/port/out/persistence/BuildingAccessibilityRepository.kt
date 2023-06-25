package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.stdlib.geography.EupMyeonDong

interface BuildingAccessibilityRepository : EntityRepository<BuildingAccessibility, String> {
    fun findByBuildingIds(buildingIds: Collection<String>): List<BuildingAccessibility>
    fun findByBuildingId(buildingId: String): BuildingAccessibility?
    fun findByPlaceIds(placeIds: Collection<String>): List<BuildingAccessibility>
    fun findByEupMyeonDong(eupMyeonDong: EupMyeonDong): List<BuildingAccessibility>
    fun countByUserId(userId: String): Int
    fun remove(id: String)
    data class CreateParams(
        val buildingId: String,
        val entranceStairInfo: StairInfo,
        val entranceImageUrls: List<String>,
        val hasSlope: Boolean,
        val hasElevator: Boolean,
        val elevatorStairInfo: StairInfo,
        val elevatorImageUrls: List<String>,
        val userId: String?,
    )
}
