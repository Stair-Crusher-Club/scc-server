package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.domain.service.TokenManager
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.reflect.KClass

@Component
class JacksonTokenManager : TokenManager {
    private val objectMapper = jacksonObjectMapper()

    override fun issueToken(content: Any): String {
        return objectMapper.writeValueAsString(content)
    }

    override fun <T : Any> verify(token: String, contentClass: KClass<T>): T {
        return objectMapper.readValue(token, contentClass.java)
    }
}
