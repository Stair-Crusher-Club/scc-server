package club.staircrusher.application.server_event.port.out.persistence

import club.staircrusher.domain.server_event.ServerEvent

interface ServerEventRepository {
    fun save(entity: ServerEvent): ServerEvent
}
