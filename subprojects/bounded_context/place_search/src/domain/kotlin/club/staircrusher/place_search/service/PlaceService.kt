package club.staircrusher.place_search.service

import club.staircrusher.place_search.model.Place

interface PlaceService {
    suspend fun findByKeyword(keyword: String): List<Place>
}