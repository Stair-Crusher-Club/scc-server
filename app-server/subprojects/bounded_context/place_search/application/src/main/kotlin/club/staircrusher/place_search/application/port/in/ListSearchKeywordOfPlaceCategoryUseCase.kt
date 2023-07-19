package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.place.PlaceCategory

@Component
class ListSearchKeywordOfPlaceCategoryUseCase(
) {
    fun handle(): List<Pair<PlaceCategory, String>> {
        return PlaceCategory.values().map {
            it to it.humanReadableName
        }
    }
}

