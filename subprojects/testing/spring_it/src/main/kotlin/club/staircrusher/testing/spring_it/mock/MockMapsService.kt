package club.staircrusher.testing.spring_it.mock

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.place.PlaceCategory
import org.springframework.context.annotation.Primary

@Component
@Primary
class MockMapsService : MapsService {
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
