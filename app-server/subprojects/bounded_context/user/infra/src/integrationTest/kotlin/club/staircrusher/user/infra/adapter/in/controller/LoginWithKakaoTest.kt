package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ApiErrorResponse
import club.staircrusher.api.spec.dto.KakaoTokensDto
import club.staircrusher.api.spec.dto.LoginResultDto
import club.staircrusher.api.spec.dto.LoginWithKakaoPostRequest
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.user.application.port.out.persistence.UserAccountConnectionRepository
import club.staircrusher.user.application.port.out.persistence.UserAuthInfoRepository
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.application.port.out.web.login.kakao.InvalidKakaoIdTokenException
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoIdToken
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
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
    lateinit var userProfileRepository: UserProfileRepository

    @Autowired
    lateinit var userAuthInfoRepository: UserAuthInfoRepository

    @Autowired
    lateinit var userAccountConnectionRepository: UserAccountConnectionRepository

    @BeforeEach
    fun setUp() {
        transactionManager.doInTransaction {
            userAuthInfoRepository.deleteAll()
            userProfileRepository.deleteAll()
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

        val anonymousUser = testDataGenerator.createAnonymousUser()
        val params = LoginWithKakaoPostRequest(
            kakaoTokens = KakaoTokensDto(
                accessToken = "dummy",
                refreshToken = "refreshToken",
                idToken = "dummy",
            )
        )

        // 첫 로그인 시도 - 회원가입
        val (userId, userAuthInfoId) = mvc
            .sccRequest("/loginWithKakao", params, anonymousUser)
            .run {
                val result = getResult(LoginResultDto::class)

                val newUserProfile = transactionManager.doInTransaction {
                    userProfileRepository.findFirstByUserId(result.user.id)!!
                }
                assertNull(newUserProfile.encryptedPassword)
                assertNull(newUserProfile.email)
                assertNull(newUserProfile.instagramId)

                val newUserAuthInfo = transactionManager.doInTransaction {
                    userAuthInfoRepository.findByUserId(newUserProfile.userId).find { it.authProviderType == UserAuthProviderType.KAKAO }
                }
                assertNotNull(newUserAuthInfo)
                assertEquals("kakaoSyncUserId", newUserAuthInfo!!.externalId)
                assertEquals("refreshToken", newUserAuthInfo.externalRefreshToken)

                Pair(newUserProfile.userId, newUserAuthInfo.id)
            }

        // 두 번째 로그인 시도 - 기 존재하는 계정에 대해 로그인
        mvc
            .sccRequest("/loginWithKakao", params, anonymousUser)
            .apply {
                val result = getResult(LoginResultDto::class)

                val userProfile = transactionManager.doInTransaction {
                    userProfileRepository.findFirstByUserId(result.user.id)!!
                }
                assertEquals(userId, userProfile.userId)
                assertNull(userProfile.email)
                assertNull(userProfile.instagramId)

                val userAuthInfo = transactionManager.doInTransaction {
                    userAuthInfoRepository.findByUserId(userProfile.userId).find { it.authProviderType == UserAuthProviderType.KAKAO }!!
                }
                assertEquals(userAuthInfoId, userAuthInfo.id)
                assertEquals("kakaoSyncUserId", userAuthInfo.externalId)
                assertEquals("refreshToken", userAuthInfo.externalRefreshToken)
            }
    }

    @Test
    fun `잘못된 kakaoTokens에 대해 INVALID_AUTHENTICATION 에러 코드가 떨어진다`() {
        Mockito.`when`(kakaoLoginService.parseIdToken("dummy")).thenThrow(InvalidKakaoIdTokenException("haha"))

        val anonymousUSer = testDataGenerator.createAnonymousUser()
        val params = LoginWithKakaoPostRequest(
            kakaoTokens = KakaoTokensDto(
                accessToken = "dummy",
                refreshToken = "refreshToken",
                idToken = "dummy",
            )
        )

        mvc
            .sccRequest("/loginWithKakao", params, anonymousUSer)
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

    @Test
    fun `비회원 계정이 생성되어 있는 상태에서 회원가입을 하면 계정 정보가 연결된다`() {
        Mockito.`when`(kakaoLoginService.parseIdToken("dummy")).thenReturn(
            KakaoIdToken(
                issuer = "https://kauth.kakao.com",
                audience = "clientId",
                expiresAtEpochSecond = SccClock.instant().epochSecond + 10,
                kakaoSyncUserId = "kakaoSyncUserId",
            )
        )

        val anonymousUser = testDataGenerator.createAnonymousUser()
        val params = LoginWithKakaoPostRequest(
            kakaoTokens = KakaoTokensDto(
                accessToken = "dummy",
                refreshToken = "refreshToken",
                idToken = "dummy",
            )
        )

        val userId = mvc
            .sccRequest("/loginWithKakao", params, anonymousUser)
            .run {
                val result = getResult(LoginResultDto::class)

                val newUserProfile = transactionManager.doInTransaction {
                    userProfileRepository.findFirstByUserId(result.user.id)!!
                }
                assertNull(newUserProfile.encryptedPassword)
                assertNull(newUserProfile.email)
                assertNull(newUserProfile.instagramId)

                val newUserAuthInfo = transactionManager.doInTransaction {
                    userAuthInfoRepository.findByUserId(newUserProfile.userId).find { it.authProviderType == UserAuthProviderType.KAKAO }
                }
                assertNotNull(newUserAuthInfo)
                assertEquals("kakaoSyncUserId", newUserAuthInfo!!.externalId)
                assertEquals("refreshToken", newUserAuthInfo.externalRefreshToken)

                newUserProfile.userId
            }

        val userAccountConnection = transactionManager.doInTransaction {
            userAccountConnectionRepository.findFirstByIdentifiedUserAccountId(userId)
        }

        assertNotNull(userAccountConnection)
        assertEquals(anonymousUser.id, userAccountConnection!!.anonymousUserAccountId)
    }

    @Test
    fun `비회원 계정 토큰이 없더라도 회원가입을 막지는 않는다`() {
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

        mvc
            .sccRequest("/loginWithKakao", params)
            .andExpect {
                status {
                    isOk()
                }
            }
    }
}
