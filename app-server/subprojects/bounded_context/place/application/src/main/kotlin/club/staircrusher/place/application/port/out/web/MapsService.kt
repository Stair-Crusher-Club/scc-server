package club.staircrusher.place.application.port.out.web

import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory

interface MapsService {
    suspend fun findAllByKeyword(
        keyword: String,
        option: SearchByKeywordOption,
    ): List<Place>

    suspend fun findFirstByKeyword(
        keyword: String,
        option: SearchByKeywordOption,
    ): Place?

    suspend fun findAllByCategory(
        category: PlaceCategory,
        option: SearchByCategoryOption,
    ): List<Place>

    data class SearchByKeywordOption(
        val region: Region? = null,
    ) {
        sealed interface Region
        data class CircleRegion(
            val centerLocation: Location,
            val radiusMeters: Int,
            val sort: Sort = Sort.ACCURACY
        ) : Region {
            enum class Sort {
                DISTANCE, // 가까운 순
                ACCURACY // 정확도 순
            }
        }

        data class RectangleRegion(
            val leftTopLocation: Location,
            val rightBottomLocation: Location,
        ) : Region
    }

    data class SearchByCategoryOption(
        val region: Region,
    ) {
        sealed interface Region
        data class CircleRegion(
            val centerLocation: Location,
            val radiusMeters: Int,
            val sort: Sort = Sort.ACCURACY
        ) : Region {
            enum class Sort {
                DISTANCE, // 가까운 순
                ACCURACY // 정확도 순
            }
        }

        data class RectangleRegion(
            val leftBottomLocation: Location,
            val rightTopLocation: Location,
        ) : Region
    }
}
