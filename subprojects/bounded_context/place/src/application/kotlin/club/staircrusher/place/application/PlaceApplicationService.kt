package club.staircrusher.place.application

import club.staircrusher.place.domain.entity.Place
import club.staircrusher.place.domain.repository.PlaceRepository

class PlaceApplicationService(
    private val placeRepository: PlaceRepository,
) {
    fun findPlace(placeId: String): Place? {
        return placeRepository.findByIdOrNull(placeId)
    }
}
