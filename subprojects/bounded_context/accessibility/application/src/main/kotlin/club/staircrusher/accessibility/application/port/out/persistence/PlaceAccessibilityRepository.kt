package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.stdlib.geography.EupMyeonDong

interface PlaceAccessibilityRepository : EntityRepository<PlaceAccessibility, String> {
    fun findByPlaceIds(placeIds: Collection<String>): List<PlaceAccessibility>
    fun findByPlaceId(placeId: String): PlaceAccessibility?
    fun findByUserId(userId: String): List<PlaceAccessibility>
    fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int
    fun countByUserId(userId: String): Int
    fun hasAccessibilityNotRegisteredPlaceInBuilding(buildingId: String): Boolean
    fun countAll(): Int
    data class CreateParams(
        val placeId: String,
        val isFirstFloor: Boolean,
        val stairInfo: StairInfo,
        val hasSlope: Boolean,
        val userId: String?,
        val imageUrls: List<String>,
    )
}
