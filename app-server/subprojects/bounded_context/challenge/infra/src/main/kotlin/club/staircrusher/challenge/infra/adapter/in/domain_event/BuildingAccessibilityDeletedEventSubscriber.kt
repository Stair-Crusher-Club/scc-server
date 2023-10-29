package club.staircrusher.challenge.infra.adapter.`in`.domain_event

import club.staircrusher.challenge.application.port.`in`.use_case.HandleBuildingAccessibilityDeletedEventUseCase
import club.staircrusher.domain_event.BuildingAccessibilityDeletedEvent
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber

@Component
class BuildingAccessibilityDeletedEventSubscriber(
    private val buildingAccessibilityDeletedEventUseCase: HandleBuildingAccessibilityDeletedEventUseCase,
) : DomainEventSubscriber<BuildingAccessibilityDeletedEvent>() {
    override fun onDomainEvent(event: BuildingAccessibilityDeletedEvent) {
        buildingAccessibilityDeletedEventUseCase.handle(event)
    }

    override fun canConsume(event: DomainEvent): Boolean {
        return event is BuildingAccessibilityDeletedEvent
    }
}
