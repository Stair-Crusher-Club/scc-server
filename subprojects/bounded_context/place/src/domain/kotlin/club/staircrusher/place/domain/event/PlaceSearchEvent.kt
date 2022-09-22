package club.staircrusher.place.domain.event

import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.domain.event.DomainEvent

data class PlaceSearchEvent(
    val searchResult: List<Place>
) : DomainEvent {

}
