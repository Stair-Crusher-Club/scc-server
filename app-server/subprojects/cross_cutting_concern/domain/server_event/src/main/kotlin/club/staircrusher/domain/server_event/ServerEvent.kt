package club.staircrusher.domain.server_event

import java.time.Instant

data class ServerEvent(
    val id: String,
    val type: ServerEventType,
    val payload: ServerEventPayload,
    val createdAt: Instant,
) {
    init {
        check(type == payload.type())
    }
}
