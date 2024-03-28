package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.EntranceDoorType
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.StairHeightLevel
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
    fun findByBuildingId(buildingId: String): List<PlaceAccessibility>
    fun countAll(): Int
    fun remove(id: String)

    data class CreateParams(
        val placeId: String,
        val userId: String?,
        val floors: List<Int>?,
        val isFirstFloor: Boolean,
        val isStairOnlyOption: Boolean?,
        val stairInfo: StairInfo,
        val stairHeightLevel: StairHeightLevel?,
        val hasSlope: Boolean,
        val entranceDoorTypes: List<EntranceDoorType>?,
        val imageUrls: List<String>,
    ) {
        fun isValid(): Boolean {
            // 0401 버전에서 추가된 항목이 모두 있거나 모두 없거나
            val isInputValid = listOf(floors, isStairOnlyOption, stairHeightLevel, entranceDoorTypes).all { it == null } || listOf(floors, isStairOnlyOption, stairHeightLevel, entranceDoorTypes).all { it != null }
            val isDoorTypesInvalid = entranceDoorTypes?.let { it.contains(EntranceDoorType.None) && it.count() > 1 }
                ?: false
            return isInputValid && isDoorTypesInvalid.not()
        }
    }
}
