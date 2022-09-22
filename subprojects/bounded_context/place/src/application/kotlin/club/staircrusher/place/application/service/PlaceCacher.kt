package club.staircrusher.place.application.service

import club.staircrusher.place.application.port.out.persistence.PlaceRepository
import club.staircrusher.place.domain.event.PlaceSearchEvent
import club.staircrusher.stdlib.domain.event.DomainEventListener
import club.staircrusher.stdlib.persistence.TransactionManager
import jakarta.inject.Named
import jakarta.inject.Singleton

@Named
@Singleton
class PlaceCacher(
    private val transactionManager: TransactionManager,
    private val placeRepository: PlaceRepository,
) : DomainEventListener<PlaceSearchEvent> {
    override fun onDomainEvent(event: PlaceSearchEvent) {
        transactionManager.doInTransaction {
            placeRepository.saveAll(event.searchResult)
        }
    }
}