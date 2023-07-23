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
        buildingRepository.saveAll(buildings)
        placeRepository.saveAll(places)
    }
}
