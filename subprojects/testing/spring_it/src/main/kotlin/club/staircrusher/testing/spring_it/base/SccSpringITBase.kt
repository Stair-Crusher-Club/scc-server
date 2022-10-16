package club.staircrusher.testing.spring_it.base

import club.staircrusher.spring_web.security.SccSecurityFilterChainConfig
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.testing.spring_it.ITDataGenerator
import club.staircrusher.user.domain.model.User
import club.staircrusher.user.domain.service.UserAuthService
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import kotlin.reflect.KClass

@SpringBootTest(classes = [SccSpringITApplication::class])
@AutoConfigureMockMvc
open class SccSpringITBase {
    @Autowired
    lateinit var transactionManager: TransactionManager

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var testDataGenerator: ITDataGenerator

    @Autowired
    lateinit var userAuthService: UserAuthService

    private val objectMapper = jacksonObjectMapper()

    protected fun MockMvc.sccRequest(url: String, requestBody: Any?, user: User? = null): ResultActionsDsl {
        return post(url) {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody?.let { objectMapper.writeValueAsBytes(it) } ?: "{}".toByteArray()
            accept = MediaType.APPLICATION_JSON_UTF8
            if (user != null) {
                val accessToken = userAuthService.issueAccessToken(user)
                header(SccSecurityFilterChainConfig.accessTokenHeader, accessToken)
            }
        }
    }

    protected fun <T : Any> ResultActionsDsl.getResult(resultClazz: KClass<T>): T {
        return objectMapper.readValue(andReturn().response.contentAsString, resultClazz.java)
    }

    protected fun <T : Any> ResultActionsDsl.getResult(typeReference: TypeReference<T>): T {
        return objectMapper.readValue(andReturn().response.contentAsString, typeReference)
    }
}
