package club.staircrusher.accessibility.infra.adapter.`in`.domain_event

import club.staircrusher.accessibility.application.port.`in`.DeleteLocalThumbnailImagesUseCase
import club.staircrusher.domain_event.AccessibilityThumbnailGeneratedEvent
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEvent
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber

@Component
class AccessibilityThumbnailGeneratedEventSubscriber(
    private val deleteLocalThumbnailImagesUseCase: DeleteLocalThumbnailImagesUseCase,
) : DomainEventSubscriber<AccessibilityThumbnailGeneratedEvent>() {
    override fun onDomainEvent(event: AccessibilityThumbnailGeneratedEvent) {
        deleteLocalThumbnailImagesUseCase.handle(event.path)
    }

    override fun canConsume(event: DomainEvent): Boolean {
        return event is AccessibilityThumbnailGeneratedEvent
    }
}
