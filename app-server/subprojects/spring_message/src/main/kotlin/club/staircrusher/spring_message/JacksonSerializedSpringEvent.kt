package club.staircrusher.spring_message

import club.staircrusher.stdlib.domain.event.DomainEvent
import org.springframework.context.ApplicationEvent

data class JacksonSerializedSpringEvent<T : DomainEvent>(
    val type: Class<T>,
    val value: String,
): ApplicationEvent(value)
