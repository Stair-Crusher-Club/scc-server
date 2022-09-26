package club.staircrusher.place_search.infra.adapter.out.web

import club.staircrusher.place_search.application.port.out.web.PlaceService
import club.staircrusher.place_search.domain.model.Building
import club.staircrusher.place_search.domain.model.Place

class InMemoryPlaceService(
    private val placeService: club.staircrusher.place.application.service.PlaceService,
) : PlaceService {
    override suspend fun findByKeyword(keyword: String): List<Place> {
        // Since `Place` class of place domain is not included in application layer,
        // it is not easy to define extension function for converting from place.Place
        // to place_search.Place. Would it be better to define data transfer object in
        // application layer so that other source set depending on it can define extension
        // method easily?
        return placeService.findByKeyword(keyword).map {
            Place(
                id = it.id,
                name = it.name,
                address = it.address.toString(),
                building = Building(
                    id = it.building!!.id,
                    address = it.building!!.address.toString(),
                ),
            )
        }
    }
}