package club.staircrusher.spring_web

import club.staircrusher.spring_web.app.SccAppAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

/**
 * SccSecurityConfig는 header에 있는 access token을 처리하는 로직만 제공하고,
 * 각 endpoint에 대한 인증 적용은 각 bounded context의 infra layer에서 별도로 제공한다.
 * 이때 access token 로직을 최우선으로 적용하기 위해 sccCommonFilterChain의 order를 highest로 둔다.
 */
@Configuration(proxyBeanMethods = false)
open class SccSecurityConfig {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    open fun sccCommonFilterChain(
        http: HttpSecurity,
        sccAppAuthenticationFilter: SccAppAuthenticationFilter,
    ): SecurityFilterChain {
        return http
            .addFilterAfter(sccAppAuthenticationFilter, BasicAuthenticationFilter::class.java)
            .build()
    }

    companion object {
        const val accessTokenHeader = "X-SCC-ACCESS-KEY"
    }
}
