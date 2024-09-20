package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.place.domain.model.Place
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.time.Instant

interface PlaceRepository : CrudRepository<Place, String> {
    fun findByBuildingId(buildingId: String): List<Place>
    @Query("""
        SELECT p.id
        FROM place p
        WHERE
            ST_Dwithin(
                location_for_query,
                ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326),
                :radiusMeters,
                false
            ) IS TRUE
    """, nativeQuery = true)
    fun findIdsByPlacesInCircle(centerLng: Double, centerLat: Double, radiusMeters: Double): List<String>

    @Query("""
        SELECT p.id
        FROM place p
        WHERE
            ST_Within(
                location_for_query,
                ST_GeomFromText(:polygonWkt, 4326)
            ) IS TRUE
    """, nativeQuery = true)
    fun findIdsByPlacesInPolygon(polygonWkt: String): List<String>

    @EntityGraph(attributePaths = ["building"])
    fun findAllByIdIn(ids: List<String>): List<Place>

    @Query("""
        SELECT p
        FROM Place p
        WHERE
            (
                (p.createdAt = :cursorCreatedAt AND p.id < :cursorId)
                OR (p.createdAt < :cursorCreatedAt)
            )
    """)
    fun findCursored(
        cursorCreatedAt: Instant,
        cursorId: String,
        pageable: Pageable,
    ): Page<Place>
}
