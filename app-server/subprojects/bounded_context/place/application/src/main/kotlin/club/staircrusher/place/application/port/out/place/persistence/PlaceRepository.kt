package club.staircrusher.place.application.port.out.place.persistence

import club.staircrusher.place.domain.model.place.Place
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

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
            )
    """, nativeQuery = true)
    fun findIdsByPlacesInCircle(centerLng: Double, centerLat: Double, radiusMeters: Double): List<String>

    @Query("""
        SELECT p.*
        FROM place p
        WHERE
            ST_Dwithin(
                location_for_query,
                ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326),
                :radiusMeters,
                false
            ) IS TRUE
    """, nativeQuery = true)
    fun findAllByPlacesInCircle(centerLng: Double, centerLat: Double, radiusMeters: Double): List<Place>

    @Query("""
        SELECT p.*
        FROM place p
        WHERE
            ST_Within(
                CAST(location_for_query AS geometry),
                ST_GeomFromText(:polygonWkt, 4326)
            )
    """, nativeQuery = true)
    fun findAllByPlacesInPolygon(polygonWkt: String): List<Place>

    @EntityGraph(attributePaths = ["building"])
    fun findAllByIdIn(ids: List<String>): List<Place>

    @EntityGraph(attributePaths = ["building"])
    fun findAllByNameStartsWithAndIsClosedFalse(name: String, pageable: Pageable): List<Place>
}
