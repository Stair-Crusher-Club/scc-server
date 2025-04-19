package club.staircrusher.place.application.port.out.accessibility.persistence

import club.staircrusher.place.domain.model.accessibility.EntranceDoorType
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.place.domain.model.accessibility.StairHeightLevel
import club.staircrusher.place.domain.model.accessibility.StairInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

@Suppress("TooManyFunctions")
interface PlaceAccessibilityRepository : CrudRepository<PlaceAccessibility, String> {
    fun findByIdIn(ids: Collection<String>): List<PlaceAccessibility>
    fun findByPlaceIdInAndDeletedAtIsNull(placeIds: Collection<String>): List<PlaceAccessibility>
    fun findFirstByPlaceIdAndDeletedAtIsNull(placeId: String): PlaceAccessibility?
    fun findByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(userId: String, from: Instant, to: Instant): List<PlaceAccessibility>
    fun findTop5ByCreatedAtAfterAndDeletedAtIsNullOrderByCreatedAtAscIdDesc(createdAt: Instant): List<PlaceAccessibility>
    fun countByUserIdAndDeletedAtIsNull(userId: String): Int
    fun countByUserIdAndCreatedAtBetweenAndDeletedAtIsNull(userId: String, from: Instant, to: Instant): Int
    @Query("""
        SELECT pa.*
        FROM place_accessibility pa
        INNER JOIN place p ON p.id = pa.place_id
        WHERE
            (:placeName IS NULL OR p.name LIKE :placeName)
            AND (cast(:createdAtFrom as timestamp) IS NULL OR pa.created_at >= :createdAtFrom)
            AND (cast(:createdAtToExclusive as timestamp) IS NULL OR pa.created_at < :createdAtToExclusive)
            AND (
                (pa.created_at = :cursorCreatedAt AND pa.id < :cursorId)
                OR (pa.created_at < :cursorCreatedAt)
            )
            AND pa.deleted_at IS NULL
        ORDER BY pa.created_at DESC, pa.id DESC
        LIMIT :limit
    """, nativeQuery = true)
    fun searchForAdmin(
        placeName: String?,
        createdAtFrom: Instant?,
        createdAtToExclusive: Instant?,
        cursorCreatedAt: Instant,
        cursorId: String,
        limit: Int,
    ): List<PlaceAccessibility>

    @Query("""
        SELECT pa
        FROM PlaceAccessibility pa
        WHERE pa.userId = :userId
            AND (
                (pa.createdAt = :cursorCreatedAt AND pa.id < :cursorId)
                OR (pa.createdAt < :cursorCreatedAt)
            )
            AND pa.deletedAt IS NULL
        ORDER BY pa.createdAt DESC, pa.id DESC
    """)
    fun findCursoredByUserId(userId: String, pageable: Pageable, cursorCreatedAt: Instant, cursorId: String): Page<PlaceAccessibility>

    fun countByDeletedAtIsNull(): Int

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
