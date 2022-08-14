package club.staircrusher.accessibility.output_adapter.service

import club.staircrusher.accessibility.domain.model.Place
import club.staircrusher.accessibility.domain.service.PlaceService
import club.staircrusher.place.application.PlaceApplicationService

class InMemoryPlaceService(
    private val placeApplicationService: PlaceApplicationService,
) : PlaceService {
    override fun findPlace(placeId: String): Place? {
        return placeApplicationService.findPlace(placeId)?.let { Place(
            id = it.id,
            buildingId = it.building.id,
        ) }
    }
}
