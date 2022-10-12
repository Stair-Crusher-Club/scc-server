package club.staircrusher.spring_message

import club.staircrusher.domain_event_api.ProtoEventConverter
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationEventPublisherAware

@Component
class SpringEventPublisher(
    private val protoEventConverter: ProtoEventConverter,
): ApplicationEventPublisherAware, DomainEventPublisher {
    lateinit var eventPublisher: ApplicationEventPublisher

    override fun setApplicationEventPublisher(applicationEventPublisher: ApplicationEventPublisher) {
        eventPublisher = applicationEventPublisher
    }

    override suspend fun publishEvent(event: DomainEvent) {
        val message = protoEventConverter.convertEventToProto(event)
        eventPublisher.publishEvent(ProtoSpringEvent(message))
    }
}
