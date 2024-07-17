package club.staircrusher.infra.server_event.out.persistence

import club.staircrusher.application.server_event.port.out.persistence.ServerEventRepository
import club.staircrusher.domain.server_event.ServerEvent
import club.staircrusher.infra.persistence.sqldelight.DB
import club.staircrusher.infra.server_event.out.persistence.sqldelight.toPersistenceModel
import club.staircrusher.stdlib.di.annotation.Component

@Component
class ServerEventRepository(
    db: DB,
) : ServerEventRepository {
    private val queries = db.serverEventQueries

    override fun save(entity: ServerEvent): ServerEvent {
        queries.save(entity.toPersistenceModel())
        return entity
    }
}
