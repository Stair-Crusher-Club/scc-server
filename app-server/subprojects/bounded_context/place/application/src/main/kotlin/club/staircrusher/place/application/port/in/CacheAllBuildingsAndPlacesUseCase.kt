package club.staircrusher.place.application.port.`in`

import club.staircrusher.place.application.port.out.persistence.BuildingRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class CacheAllBuildingsAndPlacesUseCase(
    private val transactionManager: TransactionManager,
    private val placeRepository: PlaceRepository,
    private val buildingRepository: BuildingRepository,
) {
    fun handle(buildings: Collection<Building>, places: Collection<Place>) = transactionManager.doInTransaction {
        val existingBuildingIds = buildingRepository.findAllById(buildings.map { it.id }).map { it.id }.toSet()
        buildingRepository.saveAll(buildings.filter { it.id !in existingBuildingIds })

        val existingPlaceById  = placeRepository.findAllById(places.map { it.id })
            .associateBy { it.id }
        val existingPlaceIds = existingPlaceById.values.map { it.id }.toSet()
        val newOrUpdatedPlaces = places.filter {
            val existingPlace = existingPlaceById[it.id]
            it.id !in existingPlaceIds || existingPlace?.isUpdated(it) == true
        }
        placeRepository.saveAll(newOrUpdatedPlaces)
    }
}
