package club.staircrusher.place.application.port.out.place.persistence

import club.staircrusher.place.domain.model.place.PlaceFavorite
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface PlaceFavoriteRepository : CrudRepository<PlaceFavorite, String> {
    fun findByUserIdAndDeletedAtIsNull(userId: String): List<PlaceFavorite>
    fun findFirstByUserIdAndPlaceIdAndDeletedAtIsNull(userId: String, placeId: String): PlaceFavorite?
    fun findFirstByUserIdAndPlaceId(userId: String, placeId: String): PlaceFavorite?
    fun findAllByUserIdAndPlaceIdIsInAndDeletedAtIsNull(userId: String, placeIds: Collection<String>): List<PlaceFavorite>
    fun countByPlaceIdAndDeletedAtIsNull(placeId: String): Long
    fun countByUserIdAndDeletedAtIsNull(userId: String): Long

    @Query("""
        SELECT pf
        FROM PlaceFavorite pf
        WHERE pf.userId = :userId
            AND (
                (pf.createdAt = :cursorCreatedAt AND pf.id < :cursorId)
                OR (pf.createdAt < :cursorCreatedAt)
            )
            AND pf.deletedAt IS NULL
        ORDER BY pf.createdAt DESC, pf.id DESC
    """)
    fun findCursoredByUserId(userId: String, pageable: Pageable, cursorCreatedAt: Instant, cursorId: String): Page<PlaceFavorite>
}
