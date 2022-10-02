package club.staircrusher.place_search.application.port.out.web

import club.staircrusher.place_search.domain.model.Building

interface BuildingService {
    fun getById(buildingId: String): Building?
}
