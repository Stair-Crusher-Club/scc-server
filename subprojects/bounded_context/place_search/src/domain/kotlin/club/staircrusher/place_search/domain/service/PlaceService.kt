package club.staircrusher.place_search.domain.service

import club.staircrusher.place_search.domain.model.Place

interface PlaceService {
    suspend fun findByKeyword(keyword: String): List<Place>
}