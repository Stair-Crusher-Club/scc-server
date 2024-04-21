package club.staircrusher.spring_message

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.domain.event.DomainEventSubscriber
import club.staircrusher.stdlib.persistence.TransactionManager
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.Async

@Component
open class SpringEventListener(
    private val domainEventSubscribers: List<DomainEventSubscriber<*>>,
    private val objectMapper: ObjectMapper,
    private val transactionManager: TransactionManager,
): ApplicationListener<JacksonSerializedSpringEvent<*>> {
    private val logger = KotlinLogging.logger {}

    @Suppress("TooGenericExceptionCaught")
    // FIXME: domain event 처리로 인해서 HTTP 요청 처리가 늦어지는 경우가 있어서
    //        임시로 별도 쓰레드풀에서 비동기적으로 실행하도록 조치한다.
    //        하지만 이렇게 되면 domain event 처리 로직이 domain event를 발행한 트랜잭션 바깥에서 실행되므로
    //        domain event 로직 실행에 실패할 수 있다.
    //        따라서 실패 시 재시도를 하도록 나중에 수정해줘야 한다.
    @Async("springEventListenerAsyncExecutor")
    override fun onApplicationEvent(springEvent: JacksonSerializedSpringEvent<*>) {
        val event = objectMapper.readValue(springEvent.value, springEvent.type)
        transactionManager.doInTransaction {
            domainEventSubscribers.forEach {
                try {
                    it(event)
                } catch (e: Throwable) {
                    logger.error(e) { }
                }
            }
        }
    }
}
