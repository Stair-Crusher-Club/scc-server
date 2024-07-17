package club.staircrusher.spring_web.mock

import club.staircrusher.application.server_event.port.out.persistence.ServerEventRepository
import club.staircrusher.domain.server_event.ServerEvent
import club.staircrusher.stdlib.di.annotation.Component

@Component
class MockServerEventRepository : ServerEventRepository {
    override fun save(entity: ServerEvent): ServerEvent {
        return ServerEvent(
            id = entity.id,
            type = entity.type,
            payload = entity.payload,
            createdAt = entity.createdAt
        )
    }
}
