package club.staircrusher.challenge.infra.adapter.`in`.domain_event

import club.staircrusher.challenge.application.port.`in`.use_case.HandleBuildingAccessibilityCommentDeletedEventUseCase
import club.staircrusher.domain_event.BuildingAccessibilityCommentDeletedEvent
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber

@Component
class BuildingAccessibilityCommentDeletedEventSubscriber(
    private val buildingAccessibilityCommentDeletedEventUseCase: HandleBuildingAccessibilityCommentDeletedEventUseCase,
) : DomainEventSubscriber<BuildingAccessibilityCommentDeletedEvent>() {
    override fun onDomainEvent(event: BuildingAccessibilityCommentDeletedEvent) {
        buildingAccessibilityCommentDeletedEventUseCase.handle(event)
    }

    override fun canConsume(event: DomainEvent): Boolean {
        return event is BuildingAccessibilityCommentDeletedEvent
    }
}
