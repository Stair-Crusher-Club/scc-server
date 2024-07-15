package club.staircrusher.application.server_log.port.`in`

import club.staircrusher.domain.server_log.ServerLogPayload

interface SccServerLogger {
    fun record(payload: ServerLogPayload)
}
