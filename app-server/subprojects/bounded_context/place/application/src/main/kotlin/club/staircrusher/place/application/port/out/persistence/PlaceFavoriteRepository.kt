package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.place.domain.model.PlaceFavorite
import org.springframework.data.repository.CrudRepository

interface PlaceFavoriteRepository : CrudRepository<PlaceFavorite, String> {
    fun findByUserIdAndDeletedAtIsNull(userId: String): List<PlaceFavorite>
    fun findFirstByUserIdAndPlaceIdAndDeletedAtIsNull(userId: String, placeId: String): PlaceFavorite?
    fun findFirstByUserIdAndPlaceId(userId: String, placeId: String): PlaceFavorite?
    fun findAllByUserIdAndPlaceIdIsInAndDeletedAtIsNull(userId: String, placeIds: Collection<String>): List<PlaceFavorite>
    fun countByPlaceIdAndDeletedAtIsNull(placeId: String): Long
}
