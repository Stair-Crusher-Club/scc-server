package club.staircrusher.accessibility.application.port.out.persistence

import club.staircrusher.accessibility.domain.model.AccessibilityImage
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.EntranceDoorType
import club.staircrusher.accessibility.domain.model.StairHeightLevel
import club.staircrusher.accessibility.domain.model.StairInfo
import club.staircrusher.stdlib.domain.repository.EntityRepository
import club.staircrusher.stdlib.geography.EupMyeonDong
import java.time.Instant

interface BuildingAccessibilityRepository : EntityRepository<BuildingAccessibility, String> {
    fun findByBuildingIds(buildingIds: Collection<String>): List<BuildingAccessibility>
    fun findByBuildingId(buildingId: String): BuildingAccessibility?
    fun findByPlaceIds(placeIds: Collection<String>): List<BuildingAccessibility>
    fun findByEupMyeonDong(eupMyeonDong: EupMyeonDong): List<BuildingAccessibility>
    fun findByCreatedAtGreaterThanAndOrderByCreatedAtAsc(createdAt: Instant?): List<BuildingAccessibility>
    fun updateImages(id: String, images: List<AccessibilityImage>)
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
        @Suppress("VariableNaming", "ReturnCount")
        fun isValid(): Boolean {
            // 0401 이전 버전에서는 entranceDoorTypes, entranceStairHeightLevel, elevatorStairHeightLevel 를 올려줄 수 없다.
            // entranceDoorTypes 은 필수타입이라 없으면 예전버전이라고 판단
            if (listOf(entranceDoorTypes, entranceStairHeightLevel, elevatorStairHeightLevel).all { it == null }) {
                return true
            }

            val 입구_계단이_한칸인가 = entranceStairInfo == StairInfo.ONE
            val 입구_계단_높이를_입력하지_않았다 = entranceStairHeightLevel == null
            if (입구_계단이_한칸인가 && 입구_계단_높이를_입력하지_않았다) {
                return false
            }

            val 엘리베이터_계단이_한칸인가 = elevatorStairInfo == StairInfo.ONE
            val 엘리베이터_계단_높이를_입력하지_않았다 = elevatorStairHeightLevel == null
            if (엘리베이터_계단이_한칸인가 && 엘리베이터_계단_높이를_입력하지_않았다) {
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
