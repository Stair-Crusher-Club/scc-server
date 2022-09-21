package club.staircrusher.place_search.output_adapter.service

import club.staircrusher.place.application.PlaceApplicationService
import club.staircrusher.place_search.domain.model.Building
import club.staircrusher.place_search.domain.model.Place
import club.staircrusher.place_search.domain.service.PlaceService

class InMemoryPlaceService(
    private val placeApplicationService: PlaceApplicationService,
) : PlaceService {
    override suspend fun findByKeyword(keyword: String): List<Place> {
        // Since `Place` class of place domain is not included in application layer,
        // it is not easy to define extension function for converting from place.Place
        // to place_search.Place. Would it be better to define data transfer object in
        // application layer so that other source set depending on it can define extension
        // method easily?
        return placeApplicationService.findByKeyword(keyword).map {
            Place(
                id = it.id,
                name = it.name,
                address = it.address.toString(),
                building = Building(
                    id = it.building.id,
                    address = it.building.address.toString(),
                ),
            )
        }
    }
}