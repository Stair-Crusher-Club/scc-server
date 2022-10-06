package club.staircrusher.place.infra.adapter.out.message

import club.staircrusher.stdlib.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventListener
import mu.KotlinLogging
import org.springframework.context.ApplicationListener

@Component
class SpringEventListener(
    private val domainEventListeners: List<DomainEventListener<*>>,
): ApplicationListener<DomainSpringEvent> {
    private val logger = KotlinLogging.logger {}
    override fun onApplicationEvent(event: DomainSpringEvent) {
        domainEventListeners.forEach {
            try {
                it(event.domainEvent)
            } catch (e: Throwable) {
                logger.error(e) {

                }
            }
        }
    }
}