package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.place.application.port.`in`.PlaceApplicationService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TimestampCursor
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.domain.model.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Component
class AdminSearchAccessibilitiesUseCase(
    private val placeApplicationService: PlaceApplicationService,
    private val placeAccessibilityRepository: PlaceAccessibilityRepository,
    private val buildingAccessibilityRepository: BuildingAccessibilityRepository,
    private val userAplService: UserApplicationService,
) {
    data class Result(
        val items: List<Item>,
        val cursor: String?,
    ) {
        data class Item(
            val placeAccessibility: PlaceAccessibility,
            val placeAccessibilityRegisteredUser: UserProfile?,
            val buildingAccessibility: BuildingAccessibility?,
            val buildingAccessibilityRegisteredUser: UserProfile?,
            val place: Place,
        )
    }

    fun handle(
        placeName: String?,
        createdAtFromLocalDate: LocalDate?,
        createdAtToLocalDate: LocalDate?,
        cursorValue: String?,
        limit: Int?,
    ): Result {
        val cursor = cursorValue?.let { Cursor.parse(it) } ?: Cursor.initial()
        val normalizedLimit = limit ?: DEFAULT_LIMIT

        val placeAccessibilities = placeAccessibilityRepository.searchForAdmin(
            placeName = placeName,
            createdAtFrom = createdAtFromLocalDate?.atStartOfDay(ZoneId.of("Asia/Seoul"))?.toInstant(),
            createdAtToExclusive = createdAtToLocalDate?.plusDays(1)?.atStartOfDay(ZoneId.of("Asia/Seoul"))?.toInstant(),
            cursorCreatedAt = cursor.timestamp,
            cursorId = cursor.id,
            limit = normalizedLimit + 1, // 다음 페이지가 존재하는지 확인하기 위해 한 개를 더 조회한다.
        )
        val placeById = placeApplicationService.findAllByIds(placeAccessibilities.map { it.placeId })
            .associateBy { it.id }
        val buildingAccessibilityByBuildingId = buildingAccessibilityRepository
            .findByBuildingIdInAndDeletedAtIsNull(placeById.values.map { it.building.id })
            .associateBy { it.buildingId }
        val userById = userAplService.getUserProfiles(
            userIds = placeAccessibilities.mapNotNull { it.userId } +
                buildingAccessibilityByBuildingId.values.mapNotNull { it.userId },
        ).associateBy { it.id }

        val nextCursorValue = if (placeAccessibilities.size > normalizedLimit) {
            Cursor(placeAccessibilities[normalizedLimit - 1]).value
        } else {
            null
        }

        return Result(
            items = placeAccessibilities.take(normalizedLimit).map { placeAccessibility ->
                val place = placeById[placeAccessibility.placeId]!!
                val buildingAccessibility = buildingAccessibilityByBuildingId[place.building.id]
                Result.Item(
                    placeAccessibility = placeAccessibility,
                    placeAccessibilityRegisteredUser = userById[placeAccessibility.userId],
                    buildingAccessibility = buildingAccessibility,
                    buildingAccessibilityRegisteredUser = buildingAccessibility?.userId?.let { userById[it] },
                    place = place,
                )
            },
            cursor = nextCursorValue,
        )
    }

    private data class Cursor(
        val createdAt: Instant,
        val placeAccessibilityId: String,
    ) : TimestampCursor(createdAt, placeAccessibilityId) {
        constructor(placeAccessibility: PlaceAccessibility) : this(
            createdAt = placeAccessibility.createdAt,
            placeAccessibilityId = placeAccessibility.id,
        )

        companion object {
            fun parse(cursorValue: String) = TimestampCursor.parse(cursorValue)

            fun initial() = TimestampCursor.initial()
        }
    }

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
