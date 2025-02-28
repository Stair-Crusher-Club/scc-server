package club.staircrusher.spring_web.security

import club.staircrusher.user.application.port.out.persistence.UserAccountRepository
import club.staircrusher.user.domain.model.UserAccount
import club.staircrusher.user.domain.model.UserAccountType
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
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var mvc: MockMvc

    private val identifiedUserId = "userId"
    private val anonymousUserId = "anonymousUserId"

    @Test
    fun `기본 인증 테스트`() {
        val user = getIdentifiedUser()
        val accessToken = userAuthService.issueAccessToken(user)
        mvc.get("/echoUserId/secured") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }.andExpect {
            content {
                string(identifiedUserId)
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
        val user = getIdentifiedUser()
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
                string(identifiedUserId)
            }
        }
    }

    @Test
    fun `회원가입 한 유저만 접근할 수 있는 엔드포인트 인증 테스트`() {
        val user = getIdentifiedUser()
        val accessToken = userAuthService.issueAccessToken(user)

        mvc.get("/echoUserId/secured") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }.andExpect {
            content {
                string(identifiedUserId)
            }
        }

        mvc.get("/echoUserId/identified") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }.andExpect {
            content {
                string(identifiedUserId)
            }
        }

        val anonymousUser = getAnonymousUser()
        val anonymousAccessToken = userAuthService.issueAnonymousAccessToken(anonymousUser.id)

        mvc.get("/echoUserId/secured") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $anonymousAccessToken")
        }.andExpect {
            content {
                string(anonymousUserId)
            }
        }
        mvc.get("/echoUserId/identified") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $anonymousAccessToken")
        }.andExpect {
            status {
                isForbidden()
            }
        }
    }

    private fun getIdentifiedUser(): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                id = identifiedUserId,
                accountType = UserAccountType.IDENTIFIED,
            )
        )
    }

    private fun getAnonymousUser(): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                id = anonymousUserId,
                accountType = UserAccountType.ANONYMOUS,
            )
        )
    }
}
