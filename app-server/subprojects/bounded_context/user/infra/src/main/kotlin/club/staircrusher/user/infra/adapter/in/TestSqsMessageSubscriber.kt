package club.staircrusher.user.infra.adapter.`in`

import club.staircrusher.application.message_queue.port.`in`.MessageSubscriber
import club.staircrusher.domain.message_queue.Message
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.`in`.use_case.TestSqsUseCase
import mu.KotlinLogging

@Component
class TestSqsMessageSubscriber : MessageSubscriber<TestSqsUseCase.TestMessage>() {
    private val logger = KotlinLogging.logger {}

    override fun handle(message: TestSqsUseCase.TestMessage) {
        // Handle the message here
        logger.info { "Received test sqs message" }
        logger.info { message.content }
    }

    override fun canConsume(message: Message): Boolean {
        return message is TestSqsUseCase.TestMessage
    }
}
