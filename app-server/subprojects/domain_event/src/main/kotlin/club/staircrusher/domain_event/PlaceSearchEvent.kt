package club.staircrusher.domain_event

import club.staircrusher.domain_event.dto.PlaceDTO
import club.staircrusher.stdlib.domain.event.DomainEvent

// FIXME: maybe we should not expose domain model to event
data class PlaceSearchEvent(
    val searchResult: List<PlaceDTO>
) : DomainEvent
