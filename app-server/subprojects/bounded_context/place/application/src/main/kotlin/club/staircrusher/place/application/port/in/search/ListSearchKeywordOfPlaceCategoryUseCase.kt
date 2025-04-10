package club.staircrusher.place.application.port.`in`.search

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.place.PlaceCategory

@Component
class ListSearchKeywordOfPlaceCategoryUseCase {
    fun handle(): List<Pair<PlaceCategory, String>> {
        return listOf(
            PlaceCategory.RESTAURANT,
            PlaceCategory.CAFE,
            PlaceCategory.CONVENIENCE_STORE,
            PlaceCategory.PHARMACY
        )
            .map { it to it.humanReadableName }
    }
}
