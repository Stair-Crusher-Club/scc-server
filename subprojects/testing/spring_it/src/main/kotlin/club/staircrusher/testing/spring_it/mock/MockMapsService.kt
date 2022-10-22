package club.staircrusher.testing.spring_it.mock

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.place.PlaceCategory
import org.springframework.context.annotation.Primary

@Component
@Primary
class MockMapsService : MapsService {
    override suspend fun findByKeyword(keyword: String): List<Place> {
        return emptyList()
    }

    override suspend fun findAllByKeyword(keyword: String): List<Place> {
        return emptyList()
    }

    override suspend fun findByCategory(category: PlaceCategory): List<Place> {
        return emptyList()
    }

    override suspend fun findAllByCategory(category: PlaceCategory, option: MapsService.SearchOption): List<Place> {
        return emptyList()
    }
}
