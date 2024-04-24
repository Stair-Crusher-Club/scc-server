package club.staircrusher.spring_message

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber
import club.staircrusher.stdlib.persistence.TransactionManager
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Async

@Component
open class SpringEventListener(
    private val domainEventSubscribers: List<DomainEventSubscriber<*>>,
    private val objectMapper: ObjectMapper,
    private val transactionManager: TransactionManager,
): ApplicationListener<JacksonSerializedSpringEvent<*>> {
    private val logger = KotlinLogging.logger {}

    @Suppress("TooGenericExceptionCaught")
    @Async("springEventListenerAsyncExecutor")
    @Retryable(maxAttempts = 3, backoff = Backoff(value = 1000))
    override fun onApplicationEvent(springEvent: JacksonSerializedSpringEvent<*>) {
        val event = objectMapper.readValue(springEvent.value, springEvent.type)
        transactionManager.doInTransaction {
            domainEventSubscribers.forEach {
                try {
                    it(event)
                } catch (e: Throwable) {
                    logger.error(e) { }
                    throw e
                }
            }
        }
    }

    @Recover
    fun recover(err: Throwable, springEvent: JacksonSerializedSpringEvent<*>) {
        logger.error { "Failed to process spring event of type ${springEvent.type} after retry" }
    }
}
