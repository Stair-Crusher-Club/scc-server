package club.staircrusher.spring_web.web

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

@SpringBootApplication(proxyBeanMethods = false)
@ComponentScan(
    basePackages = ["club.staircrusher"],
    includeFilters = [
        ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Component::class]),
    ],
)
open class SccExceptionHandlerTestApplication
