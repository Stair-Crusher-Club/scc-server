package club.staircrusher.stdlib.domain.event

fun interface DomainEventListener<T> where T : DomainEvent {
    fun onDomainEvent(event: T)

    operator fun invoke(event: T) = onDomainEvent(event)
}