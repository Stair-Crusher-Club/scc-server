package club.staircrusher.application.message_queue.port.out

import club.staircrusher.domain.message_queue.Message

interface MessagePublisher {
    suspend fun publish(message: Message, delaySeconds: Int? = null)
}
