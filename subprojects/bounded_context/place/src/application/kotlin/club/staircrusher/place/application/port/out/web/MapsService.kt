package club.staircrusher.place.application.port.out.web

import club.staircrusher.place.domain.model.Place
import club.staircrusher.place.domain.model.PlaceCategory

interface MapsService {
    // TODO: support filters
    suspend fun findByKeyword(
        keyword: String,
    ): List<Place>

    suspend fun findByCategory(
        category: PlaceCategory,
    ): List<Place>
}