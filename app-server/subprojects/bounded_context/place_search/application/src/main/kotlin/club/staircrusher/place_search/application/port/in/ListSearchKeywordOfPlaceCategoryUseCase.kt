package club.staircrusher.place_search.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.place.PlaceCategory

@Component
class ListSearchKeywordOfPlaceCategoryUseCase(
) {
    fun handle(): List<Pair<PlaceCategory, String>> {
        return PlaceCategory.values()
            .filter {
                listOf(
                    PlaceCategory.RESTAURANT,
                    PlaceCategory.CAFE,
                    PlaceCategory.CONVENIENCE_STORE,
                    PlaceCategory.PHARMACY
                ).contains(it)
            }
            .map { it to it.humanReadableName }
    }
}
