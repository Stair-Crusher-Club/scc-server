package club.staircrusher.accessibility.output_adapter.service

import club.staircrusher.accessibility.domain.model.Place
import club.staircrusher.accessibility.domain.service.PlaceService

class InMemoryPlaceService(
    private val placeService: club.staircrusher.place.application.service.PlaceService,
) : PlaceService {
    override fun findPlace(placeId: String): Place? {
        return placeService.findPlace(placeId)?.let { Place(
            id = it.id,
            buildingId = it.building.id,
        ) }
    }
}
