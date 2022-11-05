package club.staircrusher.place_search.application.port.out.web

import club.staircrusher.place_search.domain.model.Place

interface PlaceService {
    suspend fun findByKeyword(keyword: String): List<Place>

    suspend fun findAllByKeyword(keyword: String): List<Place>

    fun findAllByIds(ids: Collection<String>): List<Place>
    fun findByBuildingId(buildingId: String): List<Place>
}
