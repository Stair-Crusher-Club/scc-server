package club.staircrusher.challenge.infra.adapter.`in`.domain_event

import club.staircrusher.challenge.application.port.`in`.use_case.HandlePlaceAccessibilityCommentDeletedEventUseCase
import club.staircrusher.domain_event.PlaceAccessibilityCommentDeletedEvent
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber

@Component
class PlaceAccessibilityCommentDeletedEventSubscriber(
    private val placeAccessibilityCommentDeletedEventUseCase: HandlePlaceAccessibilityCommentDeletedEventUseCase,
) : DomainEventSubscriber<PlaceAccessibilityCommentDeletedEvent>() {
    override fun onDomainEvent(event: PlaceAccessibilityCommentDeletedEvent) {
        placeAccessibilityCommentDeletedEventUseCase.handle(event)
    }

    override fun canConsume(event: DomainEvent): Boolean {
        return event is PlaceAccessibilityCommentDeletedEvent
    }
}
