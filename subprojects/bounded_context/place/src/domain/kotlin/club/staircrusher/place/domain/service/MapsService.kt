package club.staircrusher.place.domain.service

import club.staircrusher.place.domain.entity.Place
import club.staircrusher.place.domain.entity.PlaceCategory
import club.staircrusher.stdlib.geography.Location

interface MapsService {
    suspend fun findByAddress(
        location: Location,
    ): List<Place>

    suspend fun findByKeyword(
        keyword: String,
    ): List<Place>

    suspend fun findByCategory(
        category: PlaceCategory,
    ): List<Place>
}