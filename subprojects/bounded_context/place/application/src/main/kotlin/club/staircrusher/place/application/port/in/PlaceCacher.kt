package club.staircrusher.place.application.port.`in`

import club.staircrusher.domain_event.PlaceSearchEvent
import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.place.application.port.out.persistence.BuildingRepository
import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.application.toBuilding
import club.staircrusher.place.application.toPlace
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class PlaceCacher(
    private val transactionManager: TransactionManager,
    private val placeRepository: PlaceRepository,
    private val buildingRepository: BuildingRepository,
) : DomainEventSubscriber<PlaceSearchEvent>() {
    override fun onDomainEvent(event: PlaceSearchEvent) {
        transactionManager.doInTransaction {
            placeRepository.saveAll(event.searchResult.map(PlaceDTO::toPlace))
            buildingRepository.saveAll(event.searchResult.mapNotNull { it.building?.toBuilding() })
        }
    }

    override fun canConsume(event: DomainEvent): Boolean {
        return event is PlaceSearchEvent
    }
}
