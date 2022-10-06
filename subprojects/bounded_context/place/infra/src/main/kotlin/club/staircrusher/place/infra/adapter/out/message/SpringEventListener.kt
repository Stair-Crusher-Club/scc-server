package club.staircrusher.place.infra.adapter.out.message

import club.staircrusher.stdlib.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventListener
import org.springframework.context.ApplicationListener

@Component
class SpringEventListener(
    private val domainEventListeners: List<DomainEventListener<*>>,
): ApplicationListener<DomainSpringEvent> {
    override fun onApplicationEvent(event: DomainSpringEvent) {
        domainEventListeners.forEach { it(event.domainEvent) }
    }
}