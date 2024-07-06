package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.PlaceFavoriteRepository
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component

@Component
class DeletePlaceFavoriteUseCase(
    private val placeFavoriteRepository: PlaceFavoriteRepository
) {
    fun handle(request: Request): Response {
        val oldPlaceFavorite = placeFavoriteRepository.findByUserIdAndPlaceId(
            userId = request.userId, placeId = request.placeId
        )
        val totalPlaceFavoriteCount = placeFavoriteRepository.countByPlaceId(request.placeId)
        if (oldPlaceFavorite == null) return Response(totalPlaceFavoriteCount = totalPlaceFavoriteCount)
        if (oldPlaceFavorite.deletedAt != null) return Response(totalPlaceFavoriteCount = totalPlaceFavoriteCount)
        oldPlaceFavorite.deletedAt = SccClock.instant()
        placeFavoriteRepository.save(oldPlaceFavorite)
        return Response(totalPlaceFavoriteCount = totalPlaceFavoriteCount)
    }

    data class Request(
        val userId: String,
        val placeId: String
    )

    data class Response(
        val totalPlaceFavoriteCount: Long
    )
}
