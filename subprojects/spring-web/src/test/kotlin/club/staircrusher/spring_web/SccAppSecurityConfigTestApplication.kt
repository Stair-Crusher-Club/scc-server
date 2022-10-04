package club.staircrusher.spring_web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import java.time.Clock

@SpringBootApplication(scanBasePackages = ["club.staircrusher"])
open class SccAppSecurityConfigTestApplication {
    @Bean
    open fun clock(): Clock {
        return Clock.systemUTC()
    }
}
