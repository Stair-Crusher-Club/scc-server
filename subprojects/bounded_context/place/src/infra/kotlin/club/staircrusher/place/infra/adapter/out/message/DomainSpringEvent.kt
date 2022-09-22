package club.staircrusher.place.infra.adapter.out.message

import club.staircrusher.stdlib.domain.event.DomainEvent
import org.springframework.context.ApplicationEvent

data class DomainSpringEvent(
    val domainEvent: DomainEvent,
): ApplicationEvent(domainEvent)