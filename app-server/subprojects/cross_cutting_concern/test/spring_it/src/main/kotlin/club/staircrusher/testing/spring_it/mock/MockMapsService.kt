package club.staircrusher.testing.spring_it.mock

import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.stdlib.place.PlaceCategory

open class MockMapsService : MapsService {
    override suspend fun findAllByKeyword(keyword: String, option: MapsService.SearchByKeywordOption): List<Place> {
        return emptyList()
    }

    override suspend fun findFirstByKeyword(keyword: String, option: MapsService.SearchByKeywordOption): Place? {
        return null
    }

    override suspend fun findAllByCategory(category: PlaceCategory, option: MapsService.SearchByCategoryOption): List<Place> {
        return emptyList()
    }
}
