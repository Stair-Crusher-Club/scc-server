package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ApiErrorResponse
import club.staircrusher.api.spec.dto.KakaoTokensDto
import club.staircrusher.api.spec.dto.LoginResultDto
import club.staircrusher.api.spec.dto.LoginWithKakaoPostRequest
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.persistence.UserRepository
import club.staircrusher.user.application.port.out.web.InvalidKakaoIdTokenException
import club.staircrusher.user.application.port.out.web.KakaoIdToken
import club.staircrusher.user.application.port.out.web.KakaoLoginService
import club.staircrusher.user.domain.model.UserAuthProviderType
import club.staircrusher.user.infra.adapter.`in`.controller.base.UserITBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean

class LoginWithKakaoTest : UserITBase() {
    @MockBean
    lateinit var kakaoLoginService: KakaoLoginService

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var userAuthInfoRepository: UserAuthInfoRepository

    @BeforeEach
    fun setUp() {
        transactionManager.doInTransaction {
            userAuthInfoRepository.removeAll()
            userRepository.removeAll()
        }
    }

    @Test
    fun `loginWithKakao API 호출을 2번하면 회원가입 - 로그인 순으로 처리된다`() {
        Mockito.`when`(kakaoLoginService.parseIdToken("dummy")).thenReturn(
            KakaoIdToken(
                issuer = "https://kauth.kakao.com",
                audience = "clientId",
                expiresAtEpochSecond = SccClock.instant().epochSecond + 10,
                kakaoSyncUserId = "kakaoSyncUserId",
            )
        )

        val params = LoginWithKakaoPostRequest(
            kakaoTokens = KakaoTokensDto(
                accessToken = "dummy",
                refreshToken = "refreshToken",
                idToken = "dummy",
            )
        )

        // 첫 로그인 시도 - 회원가입
        val (userId, userAuthInfoId) = mvc
            .sccRequest("/loginWithKakao", params)
            .run {
                val result = getResult(LoginResultDto::class)

                val newUser = transactionManager.doInTransaction {
                    userRepository.findById(result.user.id)
                }
                assertNull(newUser.encryptedPassword)
                assertNull(newUser.email)
                assertNull(newUser.instagramId)

                val newUserAuthInfo = transactionManager.doInTransaction {
                    userAuthInfoRepository.findByUserId(newUser.id).find { it.authProviderType == UserAuthProviderType.KAKAO }
                }
                assertNotNull(newUserAuthInfo)
                assertEquals("kakaoSyncUserId", newUserAuthInfo!!.externalId)
                assertEquals("refreshToken", newUserAuthInfo.externalRefreshToken)

                Pair(newUser.id, newUserAuthInfo.id)
            }

        // 두 번째 로그인 시도 - 기 존재하는 계정에 대해 로그인
        mvc
            .sccRequest("/loginWithKakao", params)
            .apply {
                val result = getResult(LoginResultDto::class)

                val user = transactionManager.doInTransaction {
                    userRepository.findById(result.user.id)
                }
                assertEquals(userId, user.id)
                assertNull(user.email)
                assertNull(user.instagramId)

                val userAuthInfo = transactionManager.doInTransaction {
                    userAuthInfoRepository.findByUserId(user.id).find { it.authProviderType == UserAuthProviderType.KAKAO }!!
                }
                assertEquals(userAuthInfoId, userAuthInfo.id)
                assertEquals("kakaoSyncUserId", userAuthInfo.externalId)
                assertEquals("refreshToken", userAuthInfo.externalRefreshToken)
            }
    }

    @Test
    fun `잘못된 kakaoTokens에 대해 INVALID_AUTHENTICATION 에러 코드가 떨어진다`() {
        Mockito.`when`(kakaoLoginService.parseIdToken("dummy")).thenThrow(InvalidKakaoIdTokenException("haha"))

        val params = LoginWithKakaoPostRequest(
            kakaoTokens = KakaoTokensDto(
                accessToken = "dummy",
                refreshToken = "refreshToken",
                idToken = "dummy",
            )
        )

        mvc
            .sccRequest("/loginWithKakao", params)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
            .apply {
                val result = getResult(ApiErrorResponse::class)
                assertEquals(ApiErrorResponse.Code.INVALID_AUTHENTICATION, result.code)
            }
    }
}
