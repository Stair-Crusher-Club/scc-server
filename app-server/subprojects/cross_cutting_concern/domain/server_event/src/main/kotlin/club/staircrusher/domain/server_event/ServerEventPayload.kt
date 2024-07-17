package club.staircrusher.domain.server_event

interface ServerEventPayload {
    fun getType(): ServerEventType
}
