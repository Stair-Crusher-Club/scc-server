package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.PlaceFavoriteRepository
import club.staircrusher.place.domain.model.PlaceFavorite
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ListPlaceFavoritesByUserUseCase(
    private val placeFavoriteRepository: PlaceFavoriteRepository
) {
    fun handle(request: Request): Response {
        val favorites = placeFavoriteRepository.findByUserId(request.userId)
        // TODO: next token 처리
        return Response(
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
