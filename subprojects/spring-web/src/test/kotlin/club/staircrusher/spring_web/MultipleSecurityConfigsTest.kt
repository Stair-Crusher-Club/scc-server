package club.staircrusher.spring_web

import club.staircrusher.user.domain.entity.User
import club.staircrusher.user.domain.service.UserAuthService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
class MultipleSecurityConfigsTest {
    @Autowired
    lateinit var userAuthService: UserAuthService

    @Autowired
    lateinit var mvc: MockMvc

    @Test
    fun test() {
        val userId = "userId"
        val accessToken = userAuthService.issueAccessToken(
            User(
                id = userId,
                nickname = "",
                encryptedPassword = "",
                instagramId = null,
                createdAt = Instant.now()
            )
        )
        mvc.get("/echoUserId/secured") {
            header(SccSecurityConfig.accessTokenHeader, accessToken)
        }.andExpect {
            content {
                string(userId)
            }
        }
        mvc.get("/echoUserId/secured").andExpect {
            status {
                isUnauthorized()
            }
        }
    }
}

@Configuration
open class SecuredEchoUserIdSecurityConfig {
    @Bean
    open fun securedEchoUserIdFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .authorizeRequests {
                it
                    .antMatchers("/echoUserId/secured").authenticated()
            }
            .build()
    }
}
