package club.staircrusher.place.application.port.out.web

import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory

interface MapsService {
    suspend fun findAllByKeyword(
        keyword: String,
        option: SearchByKeywordOption,
    ): List<Place>

    suspend fun findAllByCategory(
        category: PlaceCategory,
        option: SearchByCategoryOption,
    ): List<Place>

    data class SearchByKeywordOption(
        val region: Region? = null,
        val page: Int = 1,
    ) {
        sealed interface Region
        data class CircleRegion(
            val centerLocation: Location,
            val radiusMeters: Int,
        ) : Region
        data class RectangleRegion(
            val leftTopLocation: Location,
            val rightBottomLocation: Location,
        ) : Region
    }

    data class SearchByCategoryOption(
        val region: Region,
        val page: Int = 1,
    ) {
        sealed interface Region
        data class CircleRegion(
            val centerLocation: Location,
            val radiusMeters: Int,
        ) : Region
        data class RectangleRegion(
            val leftTopLocation: Location,
            val rightBottomLocation: Location,
        ) : Region
    }
}
