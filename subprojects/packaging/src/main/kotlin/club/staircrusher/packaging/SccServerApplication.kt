package club.staircrusher.packaging

import club.staircrusher.place.infra.adapter.out.web.KakaoProperties
import club.staircrusher.stdlib.di.annotation.Component
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType

@SpringBootApplication(proxyBeanMethods = false)
@ComponentScan(
    basePackages = ["club.staircrusher"],
    includeFilters = [
        Filter(type = FilterType.ANNOTATION, classes = [Component::class]),
    ],
)
@EnableConfigurationProperties(
    KakaoProperties::class,
)
open class SccServerApplication {
    private val logger = LoggerFactory.getLogger(javaClass)
}

fun main(args: Array<String>) {
    runApplication<SccServerApplication>(*args)
}
