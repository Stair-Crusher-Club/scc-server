package club.staircrusher.spring_web

import club.staircrusher.spring_web.app.SccAppAuthentication
import club.staircrusher.user.domain.entity.User
import club.staircrusher.user.domain.service.UserAuthService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.Instant

@SpringBootApplication(scanBasePackages = ["club.staircrusher"])
open class SccAppSecurityConfigTestApplication {
    @Bean
    open fun clock(): Clock {
        return Clock.systemUTC()
    }
}

@SpringBootTest
@AutoConfigureMockMvc
class SccAppSecurityConfigTest {
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
        mvc.get("/echoUserId") {
            header(SccSecurityConfig.accessTokenHeader, accessToken)
        }.andExpect {
            content {
                string(userId)
            }
        }
    }
}

@RestController
class EchoUserIdController {
    @GetMapping("/echoUserId")
    fun echoUserId(authentication: SccAppAuthentication): String {
        return authentication.principal
    }
}
