package club.staircrusher.domain.server_event

import com.fasterxml.jackson.annotation.JsonIgnore

interface ServerEventPayload {
    @get:JsonIgnore
    val type: ServerEventType
}
