package club.staircrusher.place.application.port.`in`.search

import club.staircrusher.place.application.port.`in`.accessibility.AccessibilityApplicationService
import club.staircrusher.place.application.port.`in`.accessibility.place_review.PlaceReviewService
import club.staircrusher.place.application.port.`in`.place.PlaceApplicationService
import club.staircrusher.place.application.result.SearchPlacesResult
import club.staircrusher.place.domain.model.accessibility.AccessibilityScore
import club.staircrusher.place.domain.model.accessibility.PlaceAccessibility
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TimestampCursor
import org.springframework.data.domain.PageRequest
import java.time.Instant

@Component
class ListConqueredPlacesQuery(
    private val transactionManager: TransactionManager,
    private val placeApplicationService: PlaceApplicationService,
    private val accessibilityApplicationService: AccessibilityApplicationService,
    private val placeReviewService: PlaceReviewService,
) {
    fun listConqueredPlaces(userId: String, limit: Long?, cursorValue: String?): Result = transactionManager.doInTransaction {
        val cursor = cursorValue?.let { Cursor.parse(it) } ?: Cursor.initial()
        val normalizedLimit = limit?.toInt() ?: DEFAULT_LIMIT
        val pageRequest = PageRequest.of(0, normalizedLimit)

        val (placeAccessibilityPage, buildingAccessibilities) = accessibilityApplicationService.findCursoredByUserId(userId, pageRequest, cursor)
        val placeAccessibilityByPlaceId = placeAccessibilityPage.content.associateBy { it.placeId }
        val buildingAccessibilityByBuildingId = buildingAccessibilities.associateBy { it.buildingId }
        val placeById = placeApplicationService.findAllByIds(placeAccessibilityByPlaceId.keys)
            .associateBy { it.id }
        val placeIdToIsFavoriteMap = placeApplicationService.isFavoritePlaces(userId = userId, placeIds = placeAccessibilityByPlaceId.keys)

        val items = placeAccessibilityByPlaceId.map { (placeId, placeAccessibility) ->
            val place = placeById[placeId]!!
            val ba = buildingAccessibilityByBuildingId[place.building.id]
            SearchPlacesResult(
                place = place,
                placeAccessibility = placeAccessibility,
                buildingAccessibility = ba,
                distance = null,
                accessibilityScore = AccessibilityScore.get(placeAccessibility, ba),
                isAccessibilityRegistrable = accessibilityApplicationService.isAccessibilityRegistrable(place),
                placeReviewCount = placeReviewService.countByPlaceId(placeId),
                isFavoritePlace = placeIdToIsFavoriteMap[placeId] ?: false
            )
        }
            .sortedByDescending { it.placeAccessibility?.createdAt }

        Result(
            items = items,
            totalCount = accessibilityApplicationService.countByUserId(userId),
            nextToken = if (placeAccessibilityPage.hasNext()) {
                Cursor(placeAccessibilityPage.content[normalizedLimit - 1]).value
            } else {
                null
            },
        )
    }

    data class Result(
        val items: List<SearchPlacesResult>,
        val totalCount: Int,
        val nextToken: String? = null,
    )

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
        private const val DEFAULT_LIMIT = 20
    }
}
