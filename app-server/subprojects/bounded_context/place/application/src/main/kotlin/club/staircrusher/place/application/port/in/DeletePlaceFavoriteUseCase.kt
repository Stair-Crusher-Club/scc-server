package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.PlaceFavoriteRepository
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component

@Component
class DeletePlaceFavoriteUseCase(
    private val placeFavoriteRepository: PlaceFavoriteRepository
) {
    fun handle(request: Request) {
        val oldPlaceFavorite = placeFavoriteRepository.findByUserIdAndPlaceId(
            userId = request.userId, placeId = request.placeId
        )
        if (oldPlaceFavorite == null) return
        if (oldPlaceFavorite.deletedAt != null) return
        oldPlaceFavorite.deletedAt = SccClock.instant()
        placeFavoriteRepository.save(oldPlaceFavorite)
    }

    data class Request(
        val userId: String,
        val placeId: String
    )
}
