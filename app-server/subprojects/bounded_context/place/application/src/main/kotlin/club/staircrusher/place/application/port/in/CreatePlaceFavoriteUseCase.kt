package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.PlaceFavoriteRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.domain.model.PlaceFavorite
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class CreatePlaceFavoriteUseCase(
    private val placeRepository: PlaceRepository,
    private val placeFavoriteRepository: PlaceFavoriteRepository,
    private val transactionManager: TransactionManager,
) {
    fun handle(request: Request): Response = transactionManager.doInTransaction {
        placeRepository.findByIdOrNull(request.placeId)
            ?: throw IllegalArgumentException("Place of id ${request.placeId} does not exist.")
        val oldPlaceFavorite =
            placeFavoriteRepository.findByUserIdAndPlaceId(userId = request.userId, placeId = request.placeId)
        if (oldPlaceFavorite?.deletedAt != null) {
            return@doInTransaction Response(
                totalPlaceFavoriteCount = placeFavoriteRepository.countByPlaceId(placeId = request.placeId),
                placeFavorite = oldPlaceFavorite
            )
        }
        val newPlaceFavorite = oldPlaceFavorite?.also {
            oldPlaceFavorite.updatedAt = SccClock.instant()
            oldPlaceFavorite.deletedAt = null
        } ?: PlaceFavorite(
            id = EntityIdGenerator.generateRandom(),
            userId = request.userId,
            placeId = request.placeId,
            createdAt = SccClock.instant(),
            updatedAt = SccClock.instant(),
            deletedAt = null
        )
        placeFavoriteRepository.save(newPlaceFavorite)
        return@doInTransaction Response(
            totalPlaceFavoriteCount = placeFavoriteRepository.countByPlaceId(placeId = request.placeId),
            placeFavorite = newPlaceFavorite
        )
    }

    data class Request(
        val userId: String,
        val placeId: String
    )

    data class Response(
        val totalPlaceFavoriteCount: Long,
        val placeFavorite: PlaceFavorite
    )
}
