package club.staircrusher.spring_message

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.context.ApplicationListener

@Component
class SpringEventListener(
    private val domainEventSubscribers: List<DomainEventSubscriber<*>>,
    private val objectMapper: ObjectMapper,
): ApplicationListener<JacksonSerializedSpringEvent<*>> {
    private val logger = KotlinLogging.logger {}

    @Suppress("TooGenericExceptionCaught")
    override fun onApplicationEvent(springEvent: JacksonSerializedSpringEvent<*>) {
        val event = objectMapper.readValue(springEvent.value, springEvent.type)
        domainEventSubscribers.forEach {
            try {
                it(event)
            } catch (e: Throwable) {
                logger.error(e) { }
            }
        }
    }
}
