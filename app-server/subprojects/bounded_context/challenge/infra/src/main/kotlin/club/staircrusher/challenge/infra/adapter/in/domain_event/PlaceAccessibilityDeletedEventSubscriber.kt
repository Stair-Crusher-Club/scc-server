package club.staircrusher.challenge.infra.adapter.`in`.domain_event

import club.staircrusher.challenge.application.port.`in`.use_case.HandlePlaceAccessibilityDeletedEventUseCase
import club.staircrusher.domain_event.PlaceAccessibilityDeletedEvent
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber

@Component
class PlaceAccessibilityDeletedEventSubscriber(
    private val placeAccessibilityDeletedEventUseCase: HandlePlaceAccessibilityDeletedEventUseCase,
) : DomainEventSubscriber<PlaceAccessibilityDeletedEvent>() {
    override fun onDomainEvent(event: PlaceAccessibilityDeletedEvent) {
        placeAccessibilityDeletedEventUseCase.handle(event)
    }

    override fun canConsume(event: DomainEvent): Boolean {
        return event is PlaceAccessibilityDeletedEvent
    }
}
