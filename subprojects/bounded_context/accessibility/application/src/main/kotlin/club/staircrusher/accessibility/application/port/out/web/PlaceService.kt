package club.staircrusher.accessibility.application.port.out.web

import club.staircrusher.accessibility.domain.model.Place

interface PlaceService {
    fun findPlace(placeId: String): Place?
}
