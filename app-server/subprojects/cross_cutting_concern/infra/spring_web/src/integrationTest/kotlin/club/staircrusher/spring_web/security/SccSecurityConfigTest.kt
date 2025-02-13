package club.staircrusher.spring_web.security

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.user.application.port.out.persistence.UserAccountRepository
import club.staircrusher.user.domain.model.UserProfile
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.domain.model.UserAccount
import club.staircrusher.user.domain.model.UserType
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
    lateinit var userAccountRepository: UserAccountRepository

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

    @Test
    fun `회원가입 한 유저만 접근할 수 있는 엔드포인트 인증 테스트`() {
        val user = getUser()
        val accessToken = userAuthService.issueAccessToken(user)
        mvc.get("/echoUserId/identified") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }.andExpect {
            content {
                string(userId)
            }
        }
        val anonymousUser = getAnonymousUser()
        val anonymousAccessToken = userAuthService.issueAccessToken(anonymousUser.id)
        mvc.get("/echoUserId/identified") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $anonymousAccessToken")
        }.andExpect {
            status {
                isForbidden()
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

    private fun getAnonymousUser(): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                id = userId,
                userType = UserType.ANONYMOUS,
                createdAt = SccClock.instant(),
                updatedAt = SccClock.instant(),
            )
        )
    }
}
