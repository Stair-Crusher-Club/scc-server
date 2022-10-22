package club.staircrusher.packaging

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ComponentScan.Filter
import org.springframework.context.annotation.FilterType

@SpringBootApplication(
    proxyBeanMethods = false,
    exclude = [DataSourceTransactionManagerAutoConfiguration::class],
)
@ComponentScan(
    basePackages = ["club.staircrusher"],
    includeFilters = [
        Filter(type = FilterType.ANNOTATION, classes = [Component::class]),
    ],
)
@ConfigurationPropertiesScan(
    basePackages = ["club.staircrusher"],
)
open class SccServerApplication

fun main(args: Array<String>) {
    runApplication<SccServerApplication>(*args)
}
