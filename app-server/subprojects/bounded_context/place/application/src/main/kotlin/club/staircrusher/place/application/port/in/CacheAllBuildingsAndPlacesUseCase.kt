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
        val existingBuildingIds = buildingRepository.findByIdIn(buildings.map { it.id }).map { it.id }.toSet()
        val existingPlaceIds = placeRepository.findByIdIn(places.map { it.id }).map { it.id }.toSet()
        buildingRepository.saveAll(buildings.filter { it.id !in existingBuildingIds })
        placeRepository.saveAll(places.filter { it.id !in existingPlaceIds })
    }
}
