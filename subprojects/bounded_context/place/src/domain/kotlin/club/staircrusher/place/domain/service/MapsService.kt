package club.staircrusher.place.domain.service

import club.staircrusher.place.domain.entity.Place
import club.staircrusher.place.domain.entity.PlaceCategory

interface MapsService {
    suspend fun findByAddress(
        address: String,
    ): List<Place>

    // TODO: support filters
    suspend fun findByKeyword(
        keyword: String,
    ): List<Place>

    suspend fun findByCategory(
        category: PlaceCategory,
    ): List<Place>
}