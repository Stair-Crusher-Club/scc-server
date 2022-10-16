package club.staircrusher.testing.spring_it.base

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

@SpringBootApplication(
    proxyBeanMethods = false,
    exclude = [DataSourceTransactionManagerAutoConfiguration::class],
)
@ComponentScan(
    basePackages = ["club.staircrusher"],
    includeFilters = [
        ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Component::class]),
    ],
)
open class SccSpringITApplication
