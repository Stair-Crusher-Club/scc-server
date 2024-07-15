package club.staircrusher.application.server_log.port.`in`

import club.staircrusher.application.server_log.port.out.persistence.ServerLogRepository
import club.staircrusher.domain.server_log.ServerLog
import club.staircrusher.domain.server_log.ServerLogPayload
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class SccServerPersistentLogger(
    private val transactionManager: TransactionManager,
    private val serverLogRepository: ServerLogRepository,
) : SccServerLogger {
    override fun record(payload: ServerLogPayload) = transactionManager.doInTransaction {
        val serverLog = ServerLog(
            id = EntityIdGenerator.generateRandom(),
            type = payload.type,
            payload = payload,
            createdAt = SccClock.instant(),
        )

        serverLogRepository.save(serverLog)
        Unit
    }
}
