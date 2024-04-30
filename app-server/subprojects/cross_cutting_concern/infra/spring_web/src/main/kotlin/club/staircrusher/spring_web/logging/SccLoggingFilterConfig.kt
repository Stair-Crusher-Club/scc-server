package club.staircrusher.spring_web.logging

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Component
@Configuration(proxyBeanMethods = false)
open class SccLoggingFilterConfig {

    @Bean
    fun sccLoggingFilterRegistrationBean(
        sccLoggingFilter: SccLoggingFilter,
    ): FilterRegistrationBean<SccLoggingFilter> {
        val registrationBean = FilterRegistrationBean<SccLoggingFilter>()
        registrationBean.filter = sccLoggingFilter
        registrationBean.order = SCC_LOGGING_FILTER_ORDER

        return registrationBean
    }

    companion object {
        const val SCC_LOGGING_FILTER_ORDER = Int.MIN_VALUE
    }
}
