package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.place.domain.model.PlaceFavorite
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceFavoriteRepository : JpaRepository<PlaceFavorite, String> {
    fun findByUserIdAndDeletedAtIsNull(userId: String): List<PlaceFavorite>
    fun findFirstByUserIdAndPlaceIdAndDeletedAtIsNull(userId: String, placeId: String): PlaceFavorite?
    fun findFirstByUserIdAndPlaceId(userId: String, placeId: String): PlaceFavorite?
    fun countByPlaceIdAndDeletedAtIsNull(placeId: String): Long
}
