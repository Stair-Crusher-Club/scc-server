package club.staircrusher.spring_web.security

import club.staircrusher.user.domain.model.UserProfile
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.domain.service.UserAuthService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class SccSecurityConfigTest {
    @Autowired
    lateinit var userAuthService: UserAuthService

    @Autowired
    lateinit var userProfileRepository: UserProfileRepository

    @Autowired
    lateinit var mvc: MockMvc

    private val userId = "userId"

    @Test
    fun `기본 인증 테스트`() {
        val user = getUser()
        val accessToken = userAuthService.issueAccessToken(user)
        mvc.get("/echoUserId/secured") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
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
            header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }.andExpect {
            status {
                isOk()
            }
            content {
                string(userId)
            }
        }
    }

    private fun getUser(): UserProfile {
        return userProfileRepository.save(
            UserProfile(
                id = userId,
                nickname = "",
                encryptedPassword = "",
                instagramId = null,
                email = "",
                mobilityTools = mutableListOf(),
            )
        )
    }
}
