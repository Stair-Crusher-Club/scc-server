package club.staircrusher.spring_web.mock

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
open class ClockConfiguration {
    @Bean
    open fun clock(): Clock {
        return Clock.systemUTC()
    }
}
