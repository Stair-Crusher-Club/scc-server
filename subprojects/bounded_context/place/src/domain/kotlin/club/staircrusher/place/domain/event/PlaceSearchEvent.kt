package club.staircrusher.place.domain.event

import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.domain.event.DomainEvent

// FIXME: maybe we should not expose domain model to event
data class PlaceSearchEvent(
    val searchResult: List<Place>
) : DomainEvent {

}
