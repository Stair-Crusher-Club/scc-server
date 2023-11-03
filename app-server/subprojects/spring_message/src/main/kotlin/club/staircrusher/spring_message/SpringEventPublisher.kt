package club.staircrusher.spring_message

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware

@Component
class SpringEventPublisher(
    private val objectMapper: ObjectMapper,
): ApplicationEventPublisherAware, DomainEventPublisher {
    lateinit var eventPublisher: ApplicationEventPublisher

    override fun setApplicationEventPublisher(applicationEventPublisher: ApplicationEventPublisher) {
        eventPublisher = applicationEventPublisher
    }

    override suspend fun publishEvent(event: DomainEvent) {
        val value = objectMapper.writeValueAsString(event)
        eventPublisher.publishEvent(JacksonSerializedSpringEvent(
            type = event.javaClass,
            value = value,
        ))
    }
}
