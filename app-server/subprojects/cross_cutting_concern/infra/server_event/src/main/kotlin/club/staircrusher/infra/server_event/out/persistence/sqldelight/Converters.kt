package club.staircrusher.infra.server_event.out.persistence.sqldelight

import club.staircrusher.domain.server_event.ServerEvent
import club.staircrusher.infra.persistence.sqldelight.migration.Server_event
import club.staircrusher.stdlib.time.toOffsetDateTime

fun ServerEvent.toPersistenceModel() = Server_event(
    id = id,
    type = type,
    payload = payload,
    created_at = createdAt.toOffsetDateTime(),
)

fun Server_event.toDomainModel() = ServerEvent(
    id = id,
    type = type,
    payload = payload,
    createdAt = created_at.toInstant(),
)
