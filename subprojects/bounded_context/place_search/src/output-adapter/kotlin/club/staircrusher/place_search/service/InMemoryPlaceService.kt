package club.staircrusher.place_search.service

import club.staircrusher.place_search.model.Place

class InMemoryPlaceService : PlaceService {
    override suspend fun findByKeyword(keyword: String): List<Place> {
        TODO("Not yet implemented")
    }
}