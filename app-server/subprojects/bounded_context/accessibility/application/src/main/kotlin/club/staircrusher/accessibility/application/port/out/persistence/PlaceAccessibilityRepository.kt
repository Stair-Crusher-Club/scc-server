package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityImage
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
    fun findOldest(): PlaceAccessibility?
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
    fun updateImages(id: String, images: List<AccessibilityImage>)
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
            // 0401 이후 버전에서는 floors, entranceDoorTypes 는 필수 / isStairOnlyOption 는 floors 에 따라, stairHeightLevel 은 stairInfo 에 따라 입력을 받는다.
            val 새로운_폼에서_필수_필드가_채워졌는가 = listOf(floors, entranceDoorTypes).all { it != null }
            if (새로운_폼에서_필수_필드가_채워졌는가.not()) {
                return false
            }

            val 복수층인가 = floors!!.size > 1
            val 다른_층으로_가는_정보를_입력하지_않았다 = isStairOnlyOption == null
            if (복수층인가 && 다른_층으로_가는_정보를_입력하지_않았다) {
                return false
            }

            val 계단이_한칸인가 = stairInfo == StairInfo.ONE
            val 계단_높이를_입력하지_않았다 = stairHeightLevel == null
            if (계단이_한칸인가 && 계단_높이를_입력하지_않았다) {
                return false
            }

            val 문이_없다 = entranceDoorTypes!!.contains(EntranceDoorType.None)
            val 문유형이_여러가지인가 = entranceDoorTypes.count() > 1
            if (문이_없다 && 문유형이_여러가지인가) {
                return false
            }
            return true
        }
    }
}
