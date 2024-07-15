package club.staircrusher.infra.server_log.out.persistence.sqldelight

import club.staircrusher.domain.server_log.ServerLog
import club.staircrusher.infra.persistence.sqldelight.migration.Server_log
import club.staircrusher.stdlib.time.toOffsetDateTime

fun ServerLog.toPersistenceModel() = Server_log(
    id = id,
    type = type,
    payload = payload,
    created_at = createdAt.toOffsetDateTime(),
)

fun Server_log.toDomainModel() = ServerLog(
    id = id,
    type = type,
    payload = payload,
    createdAt = created_at.toInstant(),
)
