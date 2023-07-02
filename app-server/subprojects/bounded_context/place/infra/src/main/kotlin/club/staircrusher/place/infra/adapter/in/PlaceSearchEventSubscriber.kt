package club.staircrusher.place.infra.adapter.`in`

import club.staircrusher.domain_event.PlaceSearchEvent
import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.place.application.port.`in`.CacheAllBuildingsAndPlacesUseCase
import club.staircrusher.place.application.toBuilding
import club.staircrusher.place.application.toPlace
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber

@Component
class PlaceSearchEventSubscriber(
    private val cacheAllBuildingsAndPlacesUseCase: CacheAllBuildingsAndPlacesUseCase,
) : DomainEventSubscriber<PlaceSearchEvent>() {
    override fun onDomainEvent(event: PlaceSearchEvent) {
        val buildings = event.searchResult.map { it.building.toBuilding() }.toSet()
        val places = event.searchResult.map(PlaceDTO::toPlace)
        cacheAllBuildingsAndPlacesUseCase.handle(buildings, places)
    }

    override fun canConsume(event: DomainEvent): Boolean {
        return event is PlaceSearchEvent
    }
}
