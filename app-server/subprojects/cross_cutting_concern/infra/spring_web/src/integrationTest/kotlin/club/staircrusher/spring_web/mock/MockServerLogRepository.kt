package club.staircrusher.spring_web.mock

import club.staircrusher.application.server_log.port.out.persistence.ServerLogRepository
import club.staircrusher.domain.server_log.ServerLog
import club.staircrusher.stdlib.di.annotation.Component

@Component
class MockServerLogRepository : ServerLogRepository {
    override fun save(entity: ServerLog): ServerLog {
        return ServerLog(
            id = entity.id,
            type = entity.type,
            payload = entity.payload,
            createdAt = entity.createdAt
        )
    }
}
