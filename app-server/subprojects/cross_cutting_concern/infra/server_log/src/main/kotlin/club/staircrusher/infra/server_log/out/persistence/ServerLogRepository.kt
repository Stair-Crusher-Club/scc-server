package club.staircrusher.infra.server_log.out.persistence

import club.staircrusher.application.server_log.port.out.persistence.ServerLogRepository
import club.staircrusher.domain.server_log.ServerLog
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.infra.server_log.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ServerLogRepository(
    db: DB,
) : ServerLogRepository {
    private val queries = db.serverLogQueries

    override fun save(entity: ServerLog): ServerLog {
        queries.save(entity.toPersistenceModel())
        return entity
    }
}
