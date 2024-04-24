package club.staircrusher.spring_message

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration(proxyBeanMethods = false)
@EnableAsync
@EnableRetry
open class SpringEventListenerAsyncExecutorConfiguration {
    @Bean
    open fun springEventListenerAsyncExecutor(): ExecutorService {
        return Executors.newCachedThreadPool()
    }
}
