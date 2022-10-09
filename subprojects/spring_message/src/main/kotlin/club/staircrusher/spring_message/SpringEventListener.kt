package club.staircrusher.spring_message

import club.staircrusher.domain_event_api.ProtoEventConverter
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber
import mu.KotlinLogging
import org.springframework.context.ApplicationListener

@Component
class SpringEventListener(
    private val domainEventSubscribers: List<DomainEventSubscriber<*>>,
    private val protoEventConverter: ProtoEventConverter,
): ApplicationListener<ProtoSpringEvent> {
    private val logger = KotlinLogging.logger {}

    override fun onApplicationEvent(springEvent: ProtoSpringEvent) {
        val event = protoEventConverter.convertProtoToEvent(springEvent.proto)
        domainEventSubscribers.forEach {
            try {
                it(event)
            } catch (e: Throwable) {
                logger.error(e) { }
            }
        }
    }
}