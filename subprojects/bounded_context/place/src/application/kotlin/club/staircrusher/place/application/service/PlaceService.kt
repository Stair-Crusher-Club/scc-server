package club.staircrusher.place.application.service

import club.staircrusher.place.domain.model.Place
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.stdlib.persistence.TransactionManager

class PlaceService(
    private val placeRepository: PlaceRepository,
    private val mapsService: MapsService,
    private val transactionManager: TransactionManager,
) {
    fun findPlace(placeId: String): Place? {
        return placeRepository.findByIdOrNull(placeId)
    }

    // TODO: support filter
    suspend fun findByKeyword(keyword: String): List<Place> {
        val places = mapsService.findByKeyword(keyword)
        transactionManager.doInTransaction {
            placeRepository.saveAll(places)
        }
        return places
    }
}
