package club.staircrusher.testing.spring_it.mock

import club.staircrusher.application.message_queue.port.out.MessagePublisher
import club.staircrusher.domain.message_queue.Message

open class MockMessagePublisher : MessagePublisher {
    override suspend fun publish(message: Message, delaySeconds: Int?) {
        return
    }
}
