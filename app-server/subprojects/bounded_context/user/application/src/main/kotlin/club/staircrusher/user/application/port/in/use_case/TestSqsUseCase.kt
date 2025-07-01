package club.staircrusher.user.application.port.`in`.use_case

import club.staircrusher.application.message_queue.port.out.MessagePublisher
import club.staircrusher.domain.message_queue.Message
import club.staircrusher.stdlib.di.annotation.Component

@Component
class TestSqsUseCase(
    private val messagePublisher: MessagePublisher,
) {
    suspend fun handle() {
        val message = TestMessage("Hello, SQS!")
        messagePublisher.publish(message)
    }

    class TestMessage(
        val content: String,
    ) : Message
}
