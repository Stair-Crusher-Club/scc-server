package club.staircrusher.stdlib.domain.event

// FIXME: make it parameterized class?
interface DomainEventPublisher {
    suspend fun publishEvent(event: DomainEvent)
}