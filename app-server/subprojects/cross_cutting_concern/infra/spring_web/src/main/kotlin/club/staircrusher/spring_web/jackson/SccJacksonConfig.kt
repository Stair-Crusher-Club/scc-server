package club.staircrusher.spring_web.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder


@Configuration(proxyBeanMethods = false)
open class SccJacksonConfig {
    @Bean
    open fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer? {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            builder
                .serializationInclusion(JsonInclude.Include.NON_ABSENT)
                .failOnUnknownProperties(false)
        }
    }

    @Bean
    fun enumFallbackModule(): FallbackEnumModule {
        return FallbackEnumModule()
    }
}
