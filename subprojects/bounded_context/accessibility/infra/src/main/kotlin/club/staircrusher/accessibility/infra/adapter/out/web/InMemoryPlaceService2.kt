package club.staircrusher.accessibility.infra.adapter.out.web

import club.staircrusher.accessibility.domain.model.Place
import club.staircrusher.accessibility.domain.service.PlaceService
import org.springframework.stereotype.Component

/**
 * Add suffix 2 in order to avoid conflicts with another InMemoryPlaceService
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
