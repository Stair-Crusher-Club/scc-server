package club.staircrusher.stdlib.domain.event

interface DomainEventListener<T> where T : DomainEvent {
    fun onDomainEvent(event: T)
}