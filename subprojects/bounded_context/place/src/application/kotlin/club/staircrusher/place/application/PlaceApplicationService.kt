package club.staircrusher.place.application

import club.staircrusher.place.domain.entity.Place
import club.staircrusher.place.domain.repository.PlaceRepository
import club.staircrusher.place.domain.service.MapsService
import club.staircrusher.stdlib.persistence.TransactionManager

class PlaceApplicationService(
    private val placeRepository: PlaceRepository,
    private val mapsService: MapsService,
    private val transactionManager: TransactionManager,
) {
    fun findPlace(placeId: String): Place? {
        return placeRepository.findByIdOrNull(placeId)
    }

    // FIXME: Should this function be suspend?
    suspend fun findByKeyword(keyword: String): List<Place> {
        val places = mapsService.findByKeyword(keyword)
        transactionManager.doInTransaction {
            placeRepository.saveAll(places)
        }
        return places
    }
}
