package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.place.domain.model.PlaceFavorite
import club.staircrusher.stdlib.domain.repository.EntityRepository

interface PlaceFavoriteRepository : EntityRepository<PlaceFavorite, String> {
    fun findByUserId(userId: String): List<PlaceFavorite>
    fun findByUserIdAndPlaceId(userId: String, placeId: String): PlaceFavorite?
    fun findByPlaceID(placeId: String): List<PlaceFavorite>
    fun countByPlaceId(placeId: String): Long
}
