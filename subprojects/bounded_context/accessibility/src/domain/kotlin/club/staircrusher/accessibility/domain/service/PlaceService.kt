package club.staircrusher.accessibility.domain.service

import club.staircrusher.accessibility.domain.model.Place

interface PlaceService {
    fun findPlace(placeId: String): Place?
}
