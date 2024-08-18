package club.staircrusher.infra.server_event.out.persistence

import org.springframework.data.repository.CrudRepository

interface JpaRdbServerEventRepository : CrudRepository<RdbServerEvent, String>
