package club.staircrusher.place.output_adapter.out.web

import club.staircrusher.place.domain.model.Place
import club.staircrusher.place.domain.model.PlaceCategory
import club.staircrusher.place.application.port.out.web.MapsService

class KakaoMapsService: MapsService {
    override suspend fun findByAddress(address: String): List<Place> {
        TODO("Not yet implemented")
    }

    override suspend fun findByKeyword(keyword: String): List<Place> {
        TODO("Not yet implemented")
    }

    override suspend fun findByCategory(category: PlaceCategory): List<Place> {
        TODO("Not yet implemented")
    }
}