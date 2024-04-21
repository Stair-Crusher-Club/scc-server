package club.staircrusher.spring_message

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Configuration(proxyBeanMethods = false)
open class SpringEventListenerAsyncExecutorConfiguration {
    @Bean
    open fun springEventListenerAsyncExecutor(): ExecutorService {
        return Executors.newCachedThreadPool()
    }
}
