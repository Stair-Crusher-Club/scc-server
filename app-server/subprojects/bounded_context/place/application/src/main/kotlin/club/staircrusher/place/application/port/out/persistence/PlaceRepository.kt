package club.staircrusher.place.application.port.out.persistence

import club.staircrusher.place.domain.model.Place
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PlaceRepository : CrudRepository<Place, String> {
    fun findByBuildingId(buildingId: String): List<Place>
    @Query("""
        SELECT *
        FROM place p
        WHERE
            ST_Dwithin(
                ST_Transform(location_for_query, 3857),
                ST_Transform(ST_SetSRID(ST_MakePoint(:centerLng, :centerLat), 4326), 3857),
                :radiusMeters
            ) IS TRUE
    """, nativeQuery = true)
    fun findByPlacesInCircle(centerLng: Double, centerLat: Double, radiusMeters: Double): List<Place>

    @Query("""
        SELECT *
        FROM place p
        WHERE
            ST_Within(
                location_for_query,
                ST_GeomFromText(:polygonWkt, 4326)
            ) IS TRUE
    """, nativeQuery = true)
    fun findByPlacesInPolygon(polygonWkt: String): List<Place>
}
