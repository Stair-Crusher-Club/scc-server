package club.staircrusher.application.server_event.port.`in`

import club.staircrusher.domain.server_event.ServerEventPayload

interface SccServerEventRecorder {
    fun record(payload: ServerEventPayload)
}
