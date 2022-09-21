package club.staircrusher.packaging

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["club.staircrusher"])
open class SccServerApplication

fun main(args: Array<String>) {
    runApplication<SccServerApplication>(*args)
}
