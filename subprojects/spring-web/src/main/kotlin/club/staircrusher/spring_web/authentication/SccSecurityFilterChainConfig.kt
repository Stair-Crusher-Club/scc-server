package club.staircrusher.spring_web.authentication

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

/**
 * FilterChainProxy의 구현 상 한 API 요청 당 사용되는 SecurityFilterChain은 한 개이다.
 * 이는, 모듈러 모노리스 구조에서 각 bounded context가 자신이 공개하는 API의 security를 제어하고 싶은 경우
 * 여러 개의 SecurityFilterChain bean을 등록하는 방식은 올바르게 동작하지 않는다는 의미이다.
 *
 * 이 문제를 해결하기 위해, 각 bounded context에서 security를 제어하기 위한 [SccSecurityConfig] interface를 정의한다.
 * 각 bounded context에서 이 타입의 bean을 등록하면, [SccSecurityFilterChainConfig]는 이 bean들에 정의된 구성을 모두 합쳐
 * 하나의 SecurityFilterChain을 bean으로 등록한다.
 */
@Configuration(proxyBeanMethods = false)
open class SccSecurityFilterChainConfig {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    open fun sccCommonFilterChain(
        http: HttpSecurity,
        sccSecurityConfigs: List<SccSecurityConfig>,
        sccAuthenticationManager: SccAuthenticationManager,
        sccAuthenticationEntryPoint: SccAuthenticationEntryPoint,
    ): SecurityFilterChain {
        return http
            .addFilterBefore(
                SccAuthenticationFilter(sccAuthenticationManager),
                BasicAuthenticationFilter::class.java,
            )
            .exceptionHandling {
                it.authenticationEntryPoint(sccAuthenticationEntryPoint)
            }
            .authorizeRequests {
                sccSecurityConfigs.forEach { sccSecurityConfig ->
                    it
                        .antMatchers(*sccSecurityConfig.getAuthenticatedUrls().toTypedArray())
                        .authenticated()
                }
                it
                    .antMatchers("/**")
                    .permitAll()
            }
            .build()
    }

    companion object {
        const val accessTokenHeader = "X-SCC-ACCESS-KEY"
    }
}
