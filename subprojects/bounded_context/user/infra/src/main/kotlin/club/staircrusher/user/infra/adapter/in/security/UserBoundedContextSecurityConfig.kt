package club.staircrusher.user.infra.adapter.`in`.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
open class UserBoundedContextSecurityConfig {
    @Bean
    open fun userBoundedContextFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeRequests {
                it
                    .antMatchers("/updateUserInfo").authenticated()
            }
            .build()
    }
}
