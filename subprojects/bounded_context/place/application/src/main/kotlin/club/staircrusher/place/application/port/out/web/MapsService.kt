package club.staircrusher.place.application.port.out.web

import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory

interface MapsService {
    // TODO: support filters
    suspend fun findByKeyword(
        keyword: String,
    ): List<Place>

    suspend fun findByCategory(
        category: PlaceCategory,
    ): List<Place>

    suspend fun findAllByCategory(
        category: PlaceCategory,
        option: SearchOption,
    ): List<Place>

    data class SearchOption(
        val region: Region,
        val page: Int = 1,
    ) {
        sealed class Region
        data class CircleRegion(
            val centerLocation: Location,
            val radiusMeters: Int,
        ) : Region()
        data class RectangleRegion(
            val leftTopLocation: Location,
            val rightBottomLocation: Location,
        ) : Region()
    }
}