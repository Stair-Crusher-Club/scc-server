package club.staircrusher.accessibility.infra.adapter.out.web

import club.staircrusher.accessibility.domain.model.Place
import club.staircrusher.accessibility.domain.service.PlaceService
import club.staircrusher.stdlib.di.annotation.Component

/**
 * FIXME: delete suffix 2 avoiding conflicts with another InMemoryPlaceService
 */
@Component
class InMemoryPlaceService2(
    private val placeService: club.staircrusher.place.application.service.PlaceService,
) : PlaceService {
    override fun findPlace(placeId: String): Place? {
        return placeService.findPlace(placeId)?.let { Place(
            id = it.id,
            buildingId = it.building!!.id,
        ) }
    }
}
