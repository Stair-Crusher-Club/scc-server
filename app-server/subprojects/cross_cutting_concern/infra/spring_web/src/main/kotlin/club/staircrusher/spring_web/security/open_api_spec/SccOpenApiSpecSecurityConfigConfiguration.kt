package club.staircrusher.spring_web.security.open_api_spec

import club.staircrusher.spring_web.security.SccSecurityConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
class SccOpenApiSpecSecurityConfigConfiguration {
    @Bean
    fun sccAppOpenApiSpecSecurityConfig(): SccSecurityConfig {
        return object : SccSecurityConfig {
            val openApiSpec = SccOpenApiSpec.fromResourcePath("/api-spec.yaml")

            override fun requestMatchers(): List<RequestMatcher> {
                return openApiSpec.paths.filter { !it.isIdentifiedUserOnly }
                    .map { it.toRequestMatcher() }
            }

            override fun identifiedUserOnlyRequestMatchers(): List<RequestMatcher> {
                return openApiSpec.paths.filter { it.isIdentifiedUserOnly }
                    .map { it.toRequestMatcher() }
            }
        }
    }

    @Bean
    fun sccAdminOpenApiSpecSecurityConfig(): SccSecurityConfig {
        return object : SccSecurityConfig {
            val openApiSpec = SccOpenApiSpec.fromResourcePath("/admin-api-spec.yaml")

            override fun requestMatchers(): List<RequestMatcher> {
                return openApiSpec.paths.filter { !it.isIdentifiedUserOnly }
                    .map { it.toRequestMatcher() }
            }

            override fun identifiedUserOnlyRequestMatchers(): List<RequestMatcher> {
                return openApiSpec.paths.filter { it.isIdentifiedUserOnly }
                    .map { it.toRequestMatcher() }
            }
        }
    }
}
