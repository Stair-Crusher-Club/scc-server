package club.staircrusher.spring_web.web

import club.staircrusher.spring_web.logging.SccLoggingInterceptor
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
@Configuration(proxyBeanMethods = false)
class SccWebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
    }

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.removeIf { it is KotlinSerializationJsonHttpMessageConverter }
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(SccLoggingInterceptor())
            .addPathPatterns("/**")
    }
}
