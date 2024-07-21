package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.KakaoTokensDto
import club.staircrusher.api.spec.dto.LoginPostRequest
import club.staircrusher.api.spec.dto.LoginResultDto
import club.staircrusher.api.spec.dto.LoginWithKakaoPostRequest
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoIdToken
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
import club.staircrusher.user.domain.model.UserAuthProviderType
import club.staircrusher.user.infra.adapter.`in`.controller.base.UserITBase
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.ResultActionsDsl

class DeleteUserTest : UserITBase() {
    @MockBean
    lateinit var kakaoLoginService: KakaoLoginService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userAuthInfoRepository: UserAuthInfoRepository

    @Test
    fun deleteUserTest() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser(
                password = "password",
            )
        }

        fun login(): ResultActionsDsl {
            return mvc
                .sccRequest("/login", LoginPostRequest(user.nickname, "password"))
        }

        login().andExpect {
            status { isNoContent() }
        }

        mvc
            .sccRequest("/deleteUser", "", user = user)
            .andExpect {
                status { isNoContent() }
                transactionManager.doInTransaction {
                    val deletedUser = userRepository.findById(user.id).get()
                    assertTrue(deletedUser.isDeleted)
                }
            }

        login().andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `소셜 로그인으로 가입한 유저 탈퇴 테스트`() {
        val params = LoginWithKakaoPostRequest(
            kakaoTokens = KakaoTokensDto(
                accessToken = "dummy",
                refreshToken = "refreshToken",
                idToken = "dummy",
            )
        )

        // given - 소셜 로그인으로 회원가입
        Mockito.`when`(kakaoLoginService.parseIdToken("dummy")).thenReturn(
            KakaoIdToken(
                issuer = "https://kauth.kakao.com",
                audience = "clientId",
                expiresAtEpochSecond = SccClock.instant().epochSecond + 10,
                kakaoSyncUserId = "kakaoSyncUserId1",
            )
        )
        val user = mvc
            .sccRequest("/loginWithKakao", params)
            .run {
                val result = getResult(LoginResultDto::class)

                val newUser = transactionManager.doInTransaction {
                    userRepository.findById(result.user.id).get()
                }

                val newUserAuthInfo = transactionManager.doInTransaction {
                    userAuthInfoRepository.findByUserId(newUser.id).find { it.authProviderType == UserAuthProviderType.KAKAO }
                }
                assertNotNull(newUserAuthInfo)

                newUser
            }

        // given - 소셜 로그인으로 한 명 더 유저를 만들어둔다.
        Mockito.`when`(kakaoLoginService.parseIdToken("dummy")).thenReturn(
            KakaoIdToken(
                issuer = "https://kauth.kakao.com",
                audience = "clientId",
                expiresAtEpochSecond = SccClock.instant().epochSecond + 10,
                kakaoSyncUserId = "kakaoSyncUserId2",
            )
        )
        val otherUser = mvc
            .sccRequest("/loginWithKakao", params)
            .run {
                val result = getResult(LoginResultDto::class)
                val otherUser = transactionManager.doInTransaction {
                    userRepository.findById(result.user.id).get()
                }
                assertNotNull(otherUser)
                assertNotEquals(user.id, otherUser.id)

                otherUser
            }

        // when
        mvc
            .sccRequest("/deleteUser", "", user = user)
            .andExpect {
                // then
                status { isNoContent() }
                transactionManager.doInTransaction {
                    val deletedUser = userRepository.findById(user.id).get()
                    assertTrue(deletedUser.isDeleted)

                    val userAuthInfo = userAuthInfoRepository.findByUserId(user.id).find { it.authProviderType == UserAuthProviderType.KAKAO }
                    assertNull(userAuthInfo)

                    // 다른 유저의 userAuthInfo는 삭제되지 않는다.
                    val otherUserAuthInfo = userAuthInfoRepository.findByUserId(otherUser.id).find { it.authProviderType == UserAuthProviderType.KAKAO }
                    assertNotNull(otherUserAuthInfo)
                }
            }
    }
}
