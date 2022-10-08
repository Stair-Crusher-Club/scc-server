package club.staircrusher.spring_message

import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware
import org.springframework.stereotype.Component

@Component
class SpringEventPublisher: ApplicationEventPublisherAware, DomainEventPublisher {
    lateinit var eventPublisher: ApplicationEventPublisher

    override fun setApplicationEventPublisher(applicationEventPublisher: ApplicationEventPublisher) {
        eventPublisher = applicationEventPublisher
    }

    override suspend fun publishEvent(event: DomainEvent) {
        eventPublisher.publishEvent(DomainSpringEvent(event))
    }
}