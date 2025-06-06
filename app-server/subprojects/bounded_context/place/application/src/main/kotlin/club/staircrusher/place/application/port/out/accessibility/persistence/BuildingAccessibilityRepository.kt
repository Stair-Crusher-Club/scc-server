package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.BuildingAccessibility
import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import club.staircrusher.place.domain.model.accessibility.StairHeightLevel
import club.staircrusher.place.domain.model.accessibility.StairInfo
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface BuildingAccessibilityRepository : CrudRepository<BuildingAccessibility, String> {
    fun findByIdIn(ids: Collection<String>): List<BuildingAccessibility>
    fun findByBuildingIdInAndDeletedAtIsNull(buildingIds: Collection<String>): List<BuildingAccessibility>
    fun findFirstByBuildingIdAndDeletedAtIsNull(buildingId: String): BuildingAccessibility?
    fun findByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(userId: String, from: Instant, to: Instant): List<BuildingAccessibility>
    fun findTop5ByCreatedAtAfterAndDeletedAtIsNullOrderByCreatedAtAscIdDesc(createdAt: Instant): List<BuildingAccessibility>
    fun countByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(userId: String, from: Instant, to: Instant): Int
    @Query("""
        SELECT ba.id
        FROM BuildingAccessibility ba
        WHERE ba.deletedAt IS NULL AND ba.createdAt > :createdAt
        ORDER BY ba.createdAt
    """)
    fun findMigrationTargets(createdAt: Instant): List<String>

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
