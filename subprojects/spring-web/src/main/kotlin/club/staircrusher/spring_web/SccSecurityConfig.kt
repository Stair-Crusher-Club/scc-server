package club.staircrusher.spring_web

import club.staircrusher.spring_web.app.SccAppAccessTokenFilter
import club.staircrusher.spring_web.app.SccAppAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@Configuration
open class SccSecurityConfig {
    @Bean
    open fun filterChain(
        http: HttpSecurity,
        sccAppAccessTokenFilter: SccAppAccessTokenFilter,
        sccAppAuthenticationFilter: SccAppAuthenticationFilter,
    ): SecurityFilterChain {
        val result = http
            .addFilterAfter(sccAppAccessTokenFilter, BasicAuthenticationFilter::class.java)
            .addFilterAfter(sccAppAuthenticationFilter, SccAppAccessTokenFilter::class.java)
            .authorizeRequests {
                it
                    .antMatchers("/updateUserInfo").authenticated()
            }
            .build()
        return result
    }

    companion object {
        const val accessTokenHeader = "X-SCC-ACCESS-KEY"
    }
}