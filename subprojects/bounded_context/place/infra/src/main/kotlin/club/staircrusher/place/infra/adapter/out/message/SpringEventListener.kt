package club.staircrusher.place.infra.adapter.out.message

import club.staircrusher.place.domain.event.PlaceSearchEvent
import club.staircrusher.stdlib.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventListener
import club.staircrusher.stdlib.util.exhaustive
import org.springframework.context.ApplicationListener

@Component
class SpringEventListener(
    private val placeSearchEventListenerList: List<DomainEventListener<PlaceSearchEvent>>,
): ApplicationListener<DomainSpringEvent> {
    override fun onApplicationEvent(event: DomainSpringEvent) {
        exhaustive-when (val domainEvent = event.domainEvent) {
            is PlaceSearchEvent -> placeSearchEventListenerList.forEach { it(domainEvent) }
            else -> throw RuntimeException("must be exhaustive")
        }
    }
}