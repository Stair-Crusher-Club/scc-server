package club.staircrusher.stdlib.domain.event

abstract class DomainEventSubscriber<T> where T : DomainEvent {
    protected abstract fun onDomainEvent(event: T)

    abstract fun canConsume(event: DomainEvent): Boolean

    @Suppress("UNCHECKED_CAST")
    operator fun invoke(event: DomainEvent) {
        if (canConsume(event)) { onDomainEvent(event as T) }
    }
}