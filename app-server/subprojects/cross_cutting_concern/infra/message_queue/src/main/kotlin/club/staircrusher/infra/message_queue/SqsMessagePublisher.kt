package club.staircrusher.infra.message_queue

import club.staircrusher.application.message_queue.port.out.MessagePublisher
import club.staircrusher.domain.message_queue.Message
import club.staircrusher.stdlib.di.annotation.Component
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.future.await
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

@Component
class SqsMessagePublisher(
    private val sqsAsyncClient: SqsAsyncClient,
    private val objectMapper: ObjectMapper,
    private val sqsProperties: SqsProperties,
) : MessagePublisher {
    private val commonQueueUrl = lazy {
        sqsAsyncClient.getQueueUrl { it.queueName(sqsProperties.commonQueueName) }
            .get()
            .queueUrl()
    }

    override suspend fun publish(message: Message, delaySeconds: Int?) {
        val messageBody = objectMapper.writeValueAsString(
            JacksonSerializedMessage(
                type = message.javaClass,
                value = objectMapper.writeValueAsString(message),
            )
        )

        val queueUrl = commonQueueUrl.value
        val sendMessageRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(messageBody)
            .delaySeconds(delaySeconds ?: 0)
            .build()

        sqsAsyncClient.sendMessage(sendMessageRequest).await()
    }
}
