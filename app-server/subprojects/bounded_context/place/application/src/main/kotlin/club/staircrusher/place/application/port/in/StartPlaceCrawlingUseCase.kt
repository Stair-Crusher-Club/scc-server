package club.staircrusher.place.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import kotlinx.coroutines.runBlocking

@Component
class StartPlaceCrawlingUseCase(
    private val placeCrawler: PlaceCrawler,
) {
    fun handle(polygon: List<Location>) {
        runBlocking {
            placeCrawler.crawlPlacesInPolygon(polygon)
        }
    }
}
