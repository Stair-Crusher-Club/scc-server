package club.staircrusher.place.output_adapter.service

import club.staircrusher.place.domain.entity.Place
import club.staircrusher.place.domain.entity.PlaceCategory
import club.staircrusher.place.domain.service.MapsService
import club.staircrusher.stdlib.geography.Location

class KakaoMapsService: MapsService {
    override suspend fun findByAddress(location: Location): List<Place> {
        TODO("Not yet implemented")
    }

    override suspend fun findByKeyword(keyword: String): List<Place> {
        TODO("Not yet implemented")
    }

    override suspend fun findByCategory(category: PlaceCategory): List<Place> {
        TODO("Not yet implemented")
    }
}