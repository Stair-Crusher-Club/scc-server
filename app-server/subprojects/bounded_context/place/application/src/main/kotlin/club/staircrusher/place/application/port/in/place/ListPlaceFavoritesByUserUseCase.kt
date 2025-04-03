package club.staircrusher.place.application.port.`in`.place

import club.staircrusher.place.application.port.out.place.persistence.PlaceFavoriteRepository
import club.staircrusher.place.domain.model.place.PlaceFavorite
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class ListPlaceFavoritesByUserUseCase(
    private val placeFavoriteRepository: PlaceFavoriteRepository,
    private val transactionManager: TransactionManager
) {
    fun handle(request: Request): Response = transactionManager.doInTransaction {
        val favorites = placeFavoriteRepository.findByUserIdAndDeletedAtIsNull(request.userId)
        // TODO: next token 처리
        return@doInTransaction Response(
            totalCount = favorites.size.toLong(),
            favorites = favorites,
            nextToken = null
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
}
