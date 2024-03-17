package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.accessibility.domain.model.BuildingAccessibility
import club.staircrusher.accessibility.domain.model.PlaceAccessibility
import club.staircrusher.place.application.port.`in`.PlaceService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.SccDomainException
import club.staircrusher.user.application.port.`in`.UserApplicationService
import club.staircrusher.user.domain.model.User
import java.time.Instant

@Component
class AdminSearchAccessibilitiesUseCase(
    private val placeService: PlaceService,
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
            val placeAccessibilityRegisteredUser: User?,
            val buildingAccessibility: BuildingAccessibility?,
            val buildingAccessibilityRegisteredUser: User?,
            val place: Place,
        )
    }

    fun handle(
        placeName: String?,
        cursorValue: String?,
        limit: Int?,
    ): Result {
        val cursor = cursorValue?.let { Cursor.parse(it) } ?: Cursor.INITIAL
        val normalizedLimit = limit ?: DEFAULT_LIMIT

        val placeAccessibilities = if (placeName != null) {
            placeAccessibilityRepository.findByPlaceNameContainsPagingByCreatedAtDesc(
                placeName = placeName,
                cursorCreatedAt = cursor.createdAt,
                cursorId = cursor.id,
                limit = normalizedLimit + 1, // 다음 페이지가 존재하는지 확인하기 위해 한 개를 더 조회한다.
            )
        } else {
            placeAccessibilityRepository.findAllPagingByCreatedAtDesc(
                cursorCreatedAt = cursor.createdAt,
                cursorId = cursor.id,
                limit = normalizedLimit + 1, // 다음 페이지가 존재하는지 확인하기 위해 한 개를 더 조회한다.
            )
        }
        val placeById = placeService.findAllByIds(placeAccessibilities.map { it.placeId })
            .associateBy { it.id }
        val buildingAccessibilityByBuildingId = buildingAccessibilityRepository
            .findByBuildingIds(placeById.values.map { it.building.id })
            .associateBy { it.buildingId }
        val userById = userAplService.getUsers(
            userIds = placeAccessibilities.mapNotNull { it.userId } +
                buildingAccessibilityByBuildingId.values.mapNotNull { it.userId },
        ).associateBy { it.id }

        val nextCursorValue = if (placeAccessibilities.size > normalizedLimit) {
            Cursor(placeAccessibilities[normalizedLimit - 1]).value
        } else {
            null
        }

        return Result(
            items = placeAccessibilities.map { placeAccessibility ->
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
        val id: String,
        val createdAt: Instant,
    ) {
        val value: String = "$id$DELIMITER${createdAt.toEpochMilli()}"

        constructor(placeAccessibility: PlaceAccessibility) : this(
            id = placeAccessibility.id,
            createdAt = placeAccessibility.createdAt,
        )

        companion object {
            private const val DELIMITER = "__"

            fun parse(cursorValue: String): Cursor {
                return try {
                    val (id, createdAtMillis) = cursorValue.split(DELIMITER)
                    Cursor(id = id, createdAt = Instant.ofEpochMilli(createdAtMillis.toLong()))
                } catch (t: Throwable) {
                    throw SccDomainException("Invalid cursor value: $cursorValue", cause = t)
                }
            }

            val INITIAL = Cursor(id = "", createdAt = Instant.EPOCH)
        }
    }

    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
