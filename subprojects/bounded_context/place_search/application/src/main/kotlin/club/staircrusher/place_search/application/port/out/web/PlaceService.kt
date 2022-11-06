package club.staircrusher.place_search.application.port.out.web

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place_search.domain.model.Place

interface PlaceService {
    suspend fun findAllByKeyword(keyword: String, option: MapsService.SearchByKeywordOption): List<Place>

    fun findAllByIds(ids: Collection<String>): List<Place>
    fun findByBuildingId(buildingId: String): List<Place>
}
