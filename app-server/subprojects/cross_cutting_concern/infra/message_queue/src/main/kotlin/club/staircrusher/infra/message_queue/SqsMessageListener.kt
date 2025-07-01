package club.staircrusher.infra.message_queue

import club.staircrusher.application.message_queue.port.`in`.MessageSubscriber
import club.staircrusher.stdlib.di.annotation.Component
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import mu.KotlinLogging
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import kotlin.coroutines.coroutineContext

@Component
class SqsMessageListener(
    private val sqsAsyncClient: SqsAsyncClient,
    private val sqsProperties: SqsProperties,
    private val messageSubscribers: List<MessageSubscriber<*>>,
    private val objectMapper: ObjectMapper,
) {
    private val logger = KotlinLogging.logger {}
    private val commonQueueUrl = lazy {
        sqsAsyncClient.getQueueUrl { it.queueName(sqsProperties.commonQueueName) }
            .get()
            .queueUrl()
    }

    @PostConstruct
    fun startListening() {
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            logger.info("Listening to SQS...")
            consumeMessage()
        }
    }

    @Suppress("NestedBlockDepth")
    private suspend fun consumeMessage() {
        while (coroutineContext.isActive) {
            try {
                val queueUrl = commonQueueUrl.value
                val receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(MAX_MESSAGE_NUM)
                    .waitTimeSeconds(WAIT_TIMEOUT_SECONDS)
                    .build()

                val response = sqsAsyncClient.receiveMessage(receiveMessageRequest).await()
                if (response.hasMessages()) {
                    logger.info { "Received ${response.messages().size} messages from SQS" }
                    response.messages().forEach {
                        processMessage(it)
                        deleteMessage(it)
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Error while receiving messages from SQS, will back off and retry" }
                delay(DELAY_ON_POLLING_ERROR_MILLIS)
            }
        }
    }

    private suspend fun processMessage(sqsMessage: Message) {
        val message = objectMapper.readValue(sqsMessage.body(), JacksonSerializedMessage::class.java)
        messageSubscribers.forEach { subscriber ->
            try {
                subscriber(objectMapper.readValue(message.value, message.type))
            } catch (e: Throwable) {
                logger.error(e) { "Error while processing message: $message" }
                throw e // Rethrow to ensure the message is not deleted if processing fails
            }
        }
    }

    private suspend fun deleteMessage(sqsMessage: Message) {
        val deleteRequest = DeleteMessageRequest.builder()
            .queueUrl(commonQueueUrl.value)
            .receiptHandle(sqsMessage.receiptHandle())
            .build()
        sqsAsyncClient.deleteMessage(deleteRequest).await()
    }

    companion object {
        private const val MAX_MESSAGE_NUM = 10
        private const val WAIT_TIMEOUT_SECONDS = 20
        private const val DELAY_ON_POLLING_ERROR_MILLIS = 5000L
    }
}
