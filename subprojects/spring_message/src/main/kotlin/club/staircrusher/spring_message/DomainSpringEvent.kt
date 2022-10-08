package club.staircrusher.spring_message

import club.staircrusher.stdlib.domain.event.DomainEvent
import org.springframework.context.ApplicationEvent

data class DomainSpringEvent(
    val domainEvent: DomainEvent,
): ApplicationEvent(domainEvent)