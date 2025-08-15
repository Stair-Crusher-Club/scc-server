package club.staircrusher.spring_web.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.hibernate.cfg.AvailableSettings
import org.hibernate.type.jackson.JacksonJsonFormatMapper
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder


@Configuration(proxyBeanMethods = false)
class SccJacksonConfig {
    @Bean
    fun jsonCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder: Jackson2ObjectMapperBuilder ->
            builder
                .serializationInclusion(JsonInclude.Include.NON_ABSENT)
                .failOnUnknownProperties(false)
        }
    }

    /**
     * Hibernate 6가 JSON/JSONB 컬럼을 처리할 때 사용할 ObjectMapper를 설정합니다.
     * KotlinModule을 포함하여 Kotlin data class의 deserialization을 지원합니다.
     */
    @Bean
    fun hibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { hibernateProperties ->
            val objectMapper = jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerKotlinModule()
                .registerModule(Hibernate6Module())
                .registerModule(JavaTimeModule())

            hibernateProperties[AvailableSettings.JSON_FORMAT_MAPPER] = JacksonJsonFormatMapper(objectMapper)
        }
    }

    @Bean
    fun enumFallbackModule(): FallbackEnumModule {
        return FallbackEnumModule()
    }
}
