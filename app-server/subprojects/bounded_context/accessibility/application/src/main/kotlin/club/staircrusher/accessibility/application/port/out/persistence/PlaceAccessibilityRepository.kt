package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.EntranceDoorType
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.accessibility.domain.model.StairHeightLevel
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.stdlib.geography.EupMyeonDong
import java.time.Instant

@Suppress("TooManyFunctions")
interface PlaceAccessibilityRepository : EntityRepository<PlaceAccessibility, String> {
    fun findByPlaceIds(placeIds: Collection<String>): List<PlaceAccessibility>
    fun findByPlaceId(placeId: String): PlaceAccessibility?
    fun findByUserId(userId: String): List<PlaceAccessibility>
    fun countByEupMyeonDong(eupMyeonDong: EupMyeonDong): Int
    fun countByUserId(userId: String): Int
    fun hasAccessibilityNotRegisteredPlaceInBuilding(buildingId: String): Boolean
    fun findByBuildingId(buildingId: String): List<PlaceAccessibility>
    fun searchForAdmin(
        placeName: String?,
        createdAtFrom: Instant?,
        createdAtToExclusive: Instant?,
        cursorCreatedAt: Instant,
        cursorId: String,
        limit: Int,
    ): List<PlaceAccessibility>
    fun countAll(): Int
    fun remove(id: String)

    data class CreateParams(
        val placeId: String,
        val userId: String?,
        val floors: List<Int>?,
        val isFirstFloor: Boolean?,
        val isStairOnlyOption: Boolean?,
        val stairInfo: StairInfo,
        val stairHeightLevel: StairHeightLevel?,
        val hasSlope: Boolean,
        val entranceDoorTypes: List<EntranceDoorType>?,
        val imageUrls: List<String>,
    ) {

        @Suppress("VariableNaming", "ReturnCount")
        fun isValid(): Boolean {
            // 0401 이전 버전에서는 floors, stairHeightLevel, entranceDoorTypes 를 올려줄 수 없다
            if (listOf(floors, isStairOnlyOption, stairHeightLevel, entranceDoorTypes).all { it == null }) {
                return true
            }
            // 0401 이후 버전에서는 floors, stairHeightLevel, entranceDoorTypes 를 모두 올려줘야한다.
            val 새로운_폼에서_필수_필드가_채워졌는가 = listOf(floors, stairHeightLevel, entranceDoorTypes).all { it != null }
            if (새로운_폼에서_필수_필드가_채워졌는가.not()) {
                return false
            }
            val 단층인가 = floors!!.size == 1
            val 복수층이면서_다른_층으로_이동하는_방법이_있는가 = floors.let { it.size > 1 && isStairOnlyOption != null }
            if ((단층인가 || 복수층이면서_다른_층으로_이동하는_방법이_있는가).not()) {
                return false
            }
            val 문유형을_등록했는가 = entranceDoorTypes!!.isEmpty()
            val 문유형_없음은_다른_문유형과_같이_등록할_수_없다 = entranceDoorTypes.isNotEmpty() && entranceDoorTypes.let { it.contains(EntranceDoorType.None) && it.size > 1 }.not()
            if ((문유형을_등록했는가 || 문유형_없음은_다른_문유형과_같이_등록할_수_없다).not()) {
                return false
            }
            return true
        }
    }
}
