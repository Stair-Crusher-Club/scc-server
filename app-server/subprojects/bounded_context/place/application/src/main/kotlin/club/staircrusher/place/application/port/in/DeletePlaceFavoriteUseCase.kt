package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.PlaceFavoriteRepository
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class DeletePlaceFavoriteUseCase(
    private val placeFavoriteRepository: PlaceFavoriteRepository,
    private val transactionManager: TransactionManager,
) {
    fun handle(request: Request): Response = transactionManager.doInTransaction {
        val oldPlaceFavorite = placeFavoriteRepository.findByUserIdAndPlaceId(
            userId = request.userId, placeId = request.placeId
        )
        if (oldPlaceFavorite == null || oldPlaceFavorite.deletedAt != null) {
            val totalPlaceFavoriteCount = placeFavoriteRepository.countByPlaceId(request.placeId)
            return@doInTransaction Response(totalPlaceFavoriteCount = totalPlaceFavoriteCount)
        }
        oldPlaceFavorite.deletedAt = SccClock.instant()
        placeFavoriteRepository.save(oldPlaceFavorite)
        return@doInTransaction Response(totalPlaceFavoriteCount = placeFavoriteRepository.countByPlaceId(request.placeId))
    }

    data class Request(
        val userId: String,
        val placeId: String
    )

    data class Response(
        val totalPlaceFavoriteCount: Long
    )
}
