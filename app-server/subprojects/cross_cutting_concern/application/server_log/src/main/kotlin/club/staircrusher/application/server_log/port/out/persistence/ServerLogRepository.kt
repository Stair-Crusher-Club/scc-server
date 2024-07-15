package club.staircrusher.application.server_log.port.out.persistence

import club.staircrusher.domain.server_log.ServerLog

interface ServerLogRepository {
    fun save(entity: ServerLog): ServerLog
}
