package club.staircrusher.place.application.port.`in`.place

import club.staircrusher.place.application.port.out.place.persistence.PlaceFavoriteRepository
import club.staircrusher.place.domain.model.place.PlaceFavorite
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TimestampCursor
import club.staircrusher.stdlib.persistence.TransactionManager
import org.springframework.data.domain.PageRequest
import java.time.Instant

@Component
class ListPlaceFavoritesQuery(
    private val placeFavoriteRepository: PlaceFavoriteRepository,
    private val transactionManager: TransactionManager
) {
    fun handle(request: Request): Response = transactionManager.doInTransaction {
        val cursor = request.nextToken?.let { Cursor.parse(it) } ?: Cursor.initial()
        val normalizedLimit = request.limit?.toInt() ?: DEFAULT_LIMIT
        val pageRequest = PageRequest.of(0, normalizedLimit)

        val favoritesPage = placeFavoriteRepository.findCursoredByUserId(request.userId, pageRequest, cursor.timestamp, cursor.id)

        Response(
            totalCount = placeFavoriteRepository.countByUserIdAndDeletedAtIsNull(request.userId),
            favorites = favoritesPage.content,
            nextToken = if (favoritesPage.hasNext()) {
                Cursor(favoritesPage.content[normalizedLimit - 1]).value
            } else {
                null
            },
        )
    }

    data class Request(
        val userId: String,
        val limit: Long? = 32,
        val nextToken: String?
    )

    data class Response(
        val totalCount: Long,
        val favorites: List<PlaceFavorite>,
        val nextToken: String? = null
    )

    private data class Cursor(
        val createdAt: Instant,
        val placeFavoriteId: String,
    ) : TimestampCursor(createdAt, placeFavoriteId) {
        constructor(placeFavorite: PlaceFavorite) : this(
            createdAt = placeFavorite.createdAt,
            placeFavoriteId = placeFavorite.id,
        )

        companion object {
            fun parse(cursorValue: String) = TimestampCursor.parse(cursorValue)

            // Use a timestamp in the far future to ensure we get all favorites
            fun initial() = TimestampCursor.initial()
        }
    }

    companion object {
        private const val DEFAULT_LIMIT = 32
    }
}
