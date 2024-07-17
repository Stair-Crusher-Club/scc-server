package club.staircrusher.application.server_event.port.`in`

import club.staircrusher.application.server_event.port.out.persistence.ServerEventRepository
import club.staircrusher.domain.server_event.ServerEvent
import club.staircrusher.domain.server_event.ServerEventPayload
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.entity.EntityIdGenerator
import club.staircrusher.stdlib.persistence.TransactionManager

@Component
class SccServerPersistentEventRecorder(
    private val transactionManager: TransactionManager,
    private val serverEventRepository: ServerEventRepository,
) : SccServerEventRecorder {
    override fun record(payload: ServerEventPayload) = transactionManager.doInTransaction {
        val serverEvent = ServerEvent(
            id = EntityIdGenerator.generateRandom(),
            type = payload.type,
            payload = payload,
            createdAt = SccClock.instant(),
        )

        serverEventRepository.save(serverEvent)
        Unit
    }
}
