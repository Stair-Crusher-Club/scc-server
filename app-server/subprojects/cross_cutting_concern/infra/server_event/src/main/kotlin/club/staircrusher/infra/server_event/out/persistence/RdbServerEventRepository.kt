package club.staircrusher.infra.server_event.out.persistence

import club.staircrusher.application.server_event.port.out.persistence.ServerEventRepository
import club.staircrusher.domain.server_event.ServerEvent
import club.staircrusher.stdlib.di.annotation.Component

@Component
class RdbServerEventRepository(
    private val jpaRdbServerEventRepository: JpaRdbServerEventRepository,
) : ServerEventRepository {
    override fun save(entity: ServerEvent): ServerEvent {
        jpaRdbServerEventRepository.save(RdbServerEvent(entity))
        return entity
    }
}
