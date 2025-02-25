package club.staircrusher.testing.spring_it.base

import club.staircrusher.spring_web.security.admin.AdminAuthenticationProperties
import club.staircrusher.spring_web.security.admin.AdminAuthenticationService
import club.staircrusher.stdlib.persistence.TransactionManager
import club.staircrusher.testing.spring_it.ITDataGenerator
import club.staircrusher.testing.spring_it.mock.MockSccClock
import club.staircrusher.user.domain.model.UserAccount
import club.staircrusher.user.domain.service.UserAuthService
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import kotlin.reflect.KClass

@SpringBootTest(classes = [SccSpringITApplication::class])
@AutoConfigureMockMvc
open class SccSpringITBase {
    @Autowired
    lateinit var transactionManager: TransactionManager

    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var clock: MockSccClock

    @Autowired
    lateinit var testDataGenerator: ITDataGenerator

    @Autowired
    lateinit var userAuthService: UserAuthService

    @Autowired
    lateinit var adminAuthenticationService: AdminAuthenticationService

    @Autowired
    lateinit var adminAuthenticationProperties: AdminAuthenticationProperties

    @BeforeEach
    fun resetClock() {
        clock.reset()
    }

    protected fun MockMvc.sccRequest(url: String, requestBody: Any?, userAccount: UserAccount? = null): ResultActionsDsl {
        return post(url) {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody?.let { objectMapper.writeValueAsBytes(it) } ?: "{}".toByteArray()
            accept = MediaType.APPLICATION_JSON_UTF8
            if (userAccount != null) {
                val accessToken = userAuthService.issueAccessToken(userAccount)
                header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            }
        }
            .asyncDispatchIfNeeded()
    }

    protected fun MockMvc.sccAdminRequest(url: String, httpMethod: HttpMethod, requestBody: Any?): ResultActionsDsl {
        val dsl: MockHttpServletRequestDsl.() -> Unit = {
            contentType = MediaType.APPLICATION_JSON
            content = requestBody?.let { objectMapper.writeValueAsBytes(it) } ?: "{}".toByteArray()
            accept = MediaType.APPLICATION_JSON_UTF8

            val accessToken = adminAuthenticationService.login(
                username = adminAuthenticationProperties.username,
                password = adminAuthenticationProperties.password,
            )
            header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }
        return when (httpMethod) {
            HttpMethod.GET -> get(url, dsl = dsl)
            HttpMethod.POST -> post(url, dsl = dsl)
            HttpMethod.PUT -> put(url, dsl = dsl)
            HttpMethod.DELETE -> delete(url, dsl = dsl)
            else -> throw IllegalArgumentException("Not supported httpMethod: $httpMethod")
        }
            .asyncDispatchIfNeeded()
    }

    protected fun <T : Any> ResultActionsDsl.getResult(resultClazz: KClass<T>): T {
        return objectMapper.readValue(andReturn().response.contentAsString, resultClazz.java)
    }

    protected final inline fun <reified T : Any> ResultActionsDsl.getResult(): T {
        return objectMapper.readValue(andReturn().response.contentAsString, T::class.java)
    }

    protected fun <T : Any> ResultActionsDsl.getResult(typeReference: TypeReference<T>): T {
        return objectMapper.readValue(andReturn().response.contentAsString, typeReference)
    }

    /**
     * MockMvc를 사용하는 경우, handler method가 async result(Mono, kotlin suspend function 등)를 반환하는 경우
     * ResultActionDsl.asyncDispatch()를 호출해줘야 제대로 동작한다.
     * 하지만 위 메소드는 handler method가 async result를 반환하지 않을 때 호출하면 에러가 난다.
     * 따라서 handler method가 반환하는 것이 async result이면 asyncDispatch()를 호출해주고, 아니면 그냥 반환해준다.
     *
     * refs:https://github.com/spring-projects/spring-framework/issues/23758
     */
    private fun ResultActionsDsl.asyncDispatchIfNeeded(): ResultActionsDsl {
        return if (andReturn().request.isAsyncStarted) {
            asyncDispatch()
        } else {
            this
        }
    }

    companion object {
        val objectMapper = jacksonObjectMapper()
    }
}
