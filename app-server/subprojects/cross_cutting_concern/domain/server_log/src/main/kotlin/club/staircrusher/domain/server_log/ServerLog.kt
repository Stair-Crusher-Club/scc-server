package club.staircrusher.domain.server_log

import java.time.Instant

data class ServerLog(
    val id: String,
    val type: ServerLogType,
    val payload: ServerLogPayload,
    val createdAt: Instant,
)
