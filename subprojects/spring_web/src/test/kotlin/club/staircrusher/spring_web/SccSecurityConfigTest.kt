package club.staircrusher.spring_web

import club.staircrusher.spring_web.authentication.SccSecurityFilterChainConfig
import club.staircrusher.user.domain.model.User
import club.staircrusher.user.domain.repository.UserRepository
import club.staircrusher.user.domain.service.UserAuthService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant

@SpringBootTest
@AutoConfigureMockMvc
class SccSecurityConfigTest {
    @Autowired
    lateinit var userAuthService: UserAuthService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var mvc: MockMvc

    private val userId = "userId"

    @Test
    fun `기본 인증 테스트`() {
        val user = getUser()
        val accessToken = userAuthService.issueAccessToken(user)
        mvc.get("/echoUserId/secured") {
            header(SccSecurityFilterChainConfig.accessTokenHeader, accessToken)
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

    @Test
    fun `인증이 필요 없는 API에서도 SccAppAuthentication argument resolving이 정상적으로 동작한다`() {
        val user = getUser()
        val accessToken = userAuthService.issueAccessToken(user)
        mvc.get("/echoUserId").andExpect {
            content {
                string("No authentication found.")
            }
        }
        mvc.get("/echoUserId") {
            header(SccSecurityFilterChainConfig.accessTokenHeader, accessToken)
        }.andExpect {
            content {
                string(userId)
            }
        }
    }

    private fun getUser(): User {
        return userRepository.save(User(
            id = userId,
            nickname = "",
            encryptedPassword = "",
            instagramId = null,
            createdAt = Instant.now()
        ))
    }
}
