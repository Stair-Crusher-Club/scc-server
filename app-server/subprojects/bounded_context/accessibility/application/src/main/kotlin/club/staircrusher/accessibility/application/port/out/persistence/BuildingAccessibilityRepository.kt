package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.EntranceDoorType
import club.staircrusher.accessibility.domain.model.StairHeightLevel
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
        val entranceStairHeightLevel: StairHeightLevel?,
        val entranceImageUrls: List<String>,
        val hasSlope: Boolean,
        val hasElevator: Boolean,
        val entranceDoorTypes: List<EntranceDoorType>?,
        val elevatorStairInfo: StairInfo,
        val elevatorStairHeightLevel: StairHeightLevel?,
        val elevatorImageUrls: List<String>,
        val userId: String?,
    ) {
        fun isValid(): Boolean {
            // 0401 버전에서 추가된 항목이 모두 있거나 모두 없거나
            val isInputValid = listOf(entranceStairHeightLevel, entranceDoorTypes, elevatorStairHeightLevel).all { it == null } || listOf(entranceStairHeightLevel, entranceDoorTypes, elevatorStairHeightLevel).all { it != null }
            val isDoorTypesInvalid = entranceDoorTypes?.isEmpty() == true || entranceDoorTypes?.let { it.contains(EntranceDoorType.None) && it.count() > 1 } ?: false
            return isInputValid && isDoorTypesInvalid.not()
        }
    }
}
