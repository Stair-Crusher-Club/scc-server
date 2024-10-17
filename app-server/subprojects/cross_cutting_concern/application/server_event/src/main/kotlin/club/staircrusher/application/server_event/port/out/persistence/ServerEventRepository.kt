package club.staircrusher.application.server_event.port.out.persistence

import club.staircrusher.domain.server_event.RdbServerEvent
import org.springframework.data.repository.CrudRepository

interface ServerEventRepository : CrudRepository<RdbServerEvent, String>
