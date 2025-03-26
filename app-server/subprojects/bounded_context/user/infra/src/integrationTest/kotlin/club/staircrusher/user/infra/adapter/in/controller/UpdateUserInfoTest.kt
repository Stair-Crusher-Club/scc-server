package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ApiErrorResponse
import club.staircrusher.api.spec.dto.UpdateUserInfoPost200Response
import club.staircrusher.api.spec.dto.UpdateUserInfoPostRequest
import club.staircrusher.api.spec.dto.UserMobilityToolDto
import club.staircrusher.application.server_event.port.`in`.SccServerEventRecorder
import club.staircrusher.domain.server_event.NewsletterSubscribedPayload
import club.staircrusher.domain.server_event.NewsletterUnsubscribedPayload
import club.staircrusher.stdlib.testing.SccRandom
import club.staircrusher.user.application.port.out.persistence.UserProfileRepository
import club.staircrusher.user.application.port.out.web.subscription.StibeeSubscriptionService
import club.staircrusher.user.domain.model.UserMobilityTool
import club.staircrusher.user.infra.adapter.`in`.controller.base.UserITBase
import club.staircrusher.user.infra.adapter.`in`.converter.toDTO
import club.staircrusher.user.infra.adapter.`in`.converter.toModel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.Year

class UpdateUserInfoTest : UserITBase() {
    @Autowired
    lateinit var userProfileRepository: UserProfileRepository

    @MockBean
    lateinit var stibeeSubscriptionService: StibeeSubscriptionService

    @MockBean
    lateinit var sccServerEventRecorder: SccServerEventRecorder

    @Test
    fun updateUserInfoTest() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val changedNickname = SccRandom.string(32)
        val changedInstagramId = SccRandom.string(32)
        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val changedMobilityTools = listOf(
            UserMobilityTool.ELECTRIC_WHEELCHAIR,
            UserMobilityTool.WALKING_ASSISTANCE_DEVICE,
        )
        val changedBirthYear = SccRandom.int(1900, 2025)
        assertNotEquals(user.profile.nickname, changedNickname)
        assertNotEquals(user.profile.instagramId, changedInstagramId)
        assertNotEquals(user.profile.email, changedEmail)
        assertNotEquals(user.profile.mobilityTools, changedMobilityTools)
        assertNotEquals(user.profile.birthYear, changedBirthYear)

        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
            email = changedEmail,
            mobilityTools = changedMobilityTools.map { it.toDTO() },
            birthYear = changedBirthYear,
        )
        mvc
            .sccRequest("/updateUserInfo", params, userAccount = user.account)
            .apply {
                val result = getResult(UpdateUserInfoPost200Response::class)
                assertEquals(user.account.id, result.user.id)
                assertEquals(changedNickname, result.user.nickname)
                assertEquals(changedInstagramId, result.user.instagramId)
                assertEquals(changedEmail, result.user.email)
                assertEquals(changedMobilityTools.sorted(), result.user.mobilityTools.map { it.toModel() }.sorted())
                assertEquals(changedBirthYear, result.user.birthYear)
            }
    }

    @Test
    fun `현재와 같은 데이터로 업데이트가 가능하다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val params = UpdateUserInfoPostRequest(
            nickname = user.profile.nickname,
            instagramId = user.profile.instagramId,
            email = user.profile.email!!,
            mobilityTools = user.profile.mobilityTools.map { it.toDTO() },
            birthYear = user.profile.birthYear,
        )
        mvc
            .sccRequest("/updateUserInfo", params, userAccount = user.account)
            .apply {
                val result = getResult(UpdateUserInfoPost200Response::class)
                assertEquals(user.account.id, result.user.id)
                assertEquals(user.profile.nickname, result.user.nickname)
                assertEquals(user.profile.instagramId, result.user.instagramId)
                assertEquals(user.profile.email, result.user.email)
                assertEquals(user.profile.birthYear, result.user.birthYear)
            }
    }

    @Test
    fun `중복된 닉네임으로는 변경이 불가능하다`() {
        val (user, userProfile) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val (_, userProfile2) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val params = UpdateUserInfoPostRequest(
            nickname = userProfile2.nickname,
            instagramId = userProfile.instagramId,
            email = userProfile.email!!,
            mobilityTools = userProfile.mobilityTools.map { it.toDTO() },
            birthYear = userProfile.birthYear,
        )
        mvc
            .sccRequest("/updateUserInfo", params, userAccount = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
            .apply {
                val result = getResult(ApiErrorResponse::class)
                assertEquals(ApiErrorResponse.Code.INVALID_NICKNAME, result.code)
            }
    }

    @Test
    fun `중복된 이메일로는 변경이 불가능하다`() {
        val (user, userProfile) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val (user2, userProfile2) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val params = UpdateUserInfoPostRequest(
            nickname = userProfile.nickname,
            instagramId = userProfile.instagramId,
            email = userProfile2.email!!,
            mobilityTools = userProfile.mobilityTools.map { it.toDTO() },
            birthYear = userProfile.birthYear,
        )
        mvc
            .sccRequest("/updateUserInfo", params, userAccount = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
            .apply {
                val result = getResult(ApiErrorResponse::class)
                assertEquals(ApiErrorResponse.Code.INVALID_EMAIL, result.code)
            }
    }

    @Test
    fun `유효하지 않은 포맷의 이메일로는 변경이 불가능하다`() {
        val (user, userProfile) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val params = UpdateUserInfoPostRequest(
            nickname = userProfile.nickname,
            instagramId = userProfile.instagramId,
            email = "strange",
            mobilityTools = userProfile.mobilityTools.map { it.toDTO() },
            birthYear = userProfile.birthYear,
        )
        mvc
            .sccRequest("/updateUserInfo", params, userAccount = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
            .apply {
                val result = getResult(ApiErrorResponse::class)
                assertEquals(ApiErrorResponse.Code.INVALID_EMAIL, result.code)
            }
    }

    @Test
    fun `mobility tools 를 여러번 업데이트 해도 잘 저장된다`() {
        val (user, userProfile) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val params = UpdateUserInfoPostRequest(
            nickname = userProfile.nickname,
            instagramId = userProfile.instagramId,
            email = changedEmail,
            mobilityTools = listOf(UserMobilityToolDto.ELECTRIC_WHEELCHAIR, UserMobilityToolDto.PROSTHETIC_FOOT),
            birthYear = userProfile.birthYear,
        )
        mvc
            .sccRequest("/updateUserInfo", params, userAccount = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                transactionManager.doInTransaction {
                    val profile = userProfileRepository.findById(userProfile.id).get()
                    Assertions.assertEquals(2, profile.mobilityTools.size)
                }
            }

        val params2 = UpdateUserInfoPostRequest(
            nickname = userProfile.nickname,
            instagramId = userProfile.instagramId,
            email = changedEmail,
            mobilityTools = listOf(UserMobilityToolDto.MANUAL_WHEELCHAIR),
            birthYear = userProfile.birthYear,
        )
        mvc
            .sccRequest("/updateUserInfo", params2, userAccount = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                transactionManager.doInTransaction {
                    val profile = userProfileRepository.findById(userProfile.id).get()
                    Assertions.assertEquals(1, profile.mobilityTools.size)
                }
            }
    }

    @Test
    fun `뉴스레터 수신에 동의하면 stibee 에 연동한다`() {
        val (user, _) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val changedNickname = SccRandom.string(32)
        val changedInstagramId = SccRandom.string(32)
        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val changedMobilityTools = listOf(
            UserMobilityTool.ELECTRIC_WHEELCHAIR,
            UserMobilityTool.WALKING_ASSISTANCE_DEVICE,
        )
        val changedBirthYear = SccRandom.int(1900, 2025)
        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
            email = changedEmail,
            mobilityTools = changedMobilityTools.map { it.toDTO() },
            isNewsLetterSubscriptionAgreed = true,
            birthYear = changedBirthYear,
        )

        mvc
            .sccRequest("/updateUserInfo", params, userAccount = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                verifyBlocking(stibeeSubscriptionService, atLeastOnce()) { registerSubscriber(eq(changedEmail), eq(changedNickname), any()) }
                verify(sccServerEventRecorder, atLeastOnce()).record(NewsletterSubscribedPayload(user.id))
            }
    }

    @Test
    fun `뉴스레터 수신에 동의하지 않으면 stibee 에 연동하지 않는다`() {
        val (user, _) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val changedNickname = SccRandom.string(32)
        val changedInstagramId = SccRandom.string(32)
        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val changedMobilityTools = listOf(
            UserMobilityTool.ELECTRIC_WHEELCHAIR,
            UserMobilityTool.WALKING_ASSISTANCE_DEVICE,
        )
        val changedBirthYear = SccRandom.int(1900, 2025)
        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
            email = changedEmail,
            mobilityTools = changedMobilityTools.map { it.toDTO() },
            isNewsLetterSubscriptionAgreed = false,
            birthYear = changedBirthYear,
        )

        mvc
            .sccRequest("/updateUserInfo", params, userAccount = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                verifyBlocking(stibeeSubscriptionService, never()) { registerSubscriber(eq(changedEmail), eq(changedNickname), any()) }
                verify(sccServerEventRecorder, never()).record(NewsletterSubscribedPayload(user.id))
            }
    }

    @Test
    fun `명시적인 동의 없이는 stibee 에 연동하지 않는다`() {
        val (user, _) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val changedNickname = SccRandom.string(32)
        val changedInstagramId = SccRandom.string(32)
        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val changedMobilityTools = listOf(
            UserMobilityTool.ELECTRIC_WHEELCHAIR,
            UserMobilityTool.WALKING_ASSISTANCE_DEVICE,
        )
        val changedBirthYear = SccRandom.int(1900, 2025)

        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
            email = changedEmail,
            mobilityTools = changedMobilityTools.map { it.toDTO() },
            // 하위 호환성
            isNewsLetterSubscriptionAgreed = null,
            birthYear = changedBirthYear,
        )

        mvc
            .sccRequest("/updateUserInfo", params, userAccount = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                verifyBlocking(stibeeSubscriptionService, never()) { registerSubscriber(eq(changedEmail), eq(changedNickname), any()) }
                verify(sccServerEventRecorder, never()).record(NewsletterSubscribedPayload(user.id))
            }
    }

    @Test
    fun `유효하지 않은 생년으로는 변경이 불가능하다`() {
        val (user, userProfile) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        // 미래의 연도로 테스트
        val futureYear = Year.now().value + 1
        val paramsWithFutureYear = UpdateUserInfoPostRequest(
            nickname = userProfile.nickname,
            instagramId = userProfile.instagramId,
            email = userProfile.email!!,
            mobilityTools = userProfile.mobilityTools.map { it.toDTO() },
            birthYear = futureYear,
        )
        mvc
            .sccRequest("/updateUserInfo", paramsWithFutureYear, userAccount = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
            .apply {
                val result = getResult(ApiErrorResponse::class)
                assertEquals(ApiErrorResponse.Code.INVALID_BIRTH_YEAR, result.code)
            }

        // 너무 과거의 연도로 테스트 (1800년)
        val tooOldYear = 1800
        val paramsWithTooOldYear = UpdateUserInfoPostRequest(
            nickname = userProfile.nickname,
            instagramId = userProfile.instagramId,
            email = userProfile.email!!,
            mobilityTools = userProfile.mobilityTools.map { it.toDTO() },
            birthYear = tooOldYear,
        )
        mvc
            .sccRequest("/updateUserInfo", paramsWithTooOldYear, userAccount = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
            .apply {
                val result = getResult(ApiErrorResponse::class)
                assertEquals(ApiErrorResponse.Code.INVALID_BIRTH_YEAR, result.code)
            }

        // 유효한 연도로 테스트 (1950년)
        val validYear = 1950
        val paramsWithValidYear = UpdateUserInfoPostRequest(
            nickname = userProfile.nickname,
            instagramId = userProfile.instagramId,
            email = userProfile.email!!,
            mobilityTools = userProfile.mobilityTools.map { it.toDTO() },
            birthYear = validYear,
        )
        mvc
            .sccRequest("/updateUserInfo", paramsWithValidYear, userAccount = user)
            .andExpect {
                status {
                    isOk()
                }
            }
    }

    @Test
    fun `뉴스레터 수신 동의 후 취소하면 stibee 에서 구독 취소된다`() {
        val (user, _) = transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser()
        }

        val email = "${SccRandom.string(32)}@staircrusher.club"
        val nickname = SccRandom.string(32)
        
        // First subscribe to newsletter
        val subscribeParams = UpdateUserInfoPostRequest(
            nickname = nickname,
            email = email,
            isNewsLetterSubscriptionAgreed = true,
            mobilityTools = listOf(),
            birthYear = SccRandom.int(1900, 2025),
        )

        mvc
            .sccRequest("/updateUserInfo", subscribeParams, userAccount = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                verifyBlocking(stibeeSubscriptionService, atLeastOnce()) { registerSubscriber(eq(email), eq(nickname), any()) }
                verify(sccServerEventRecorder, atLeastOnce()).record(NewsletterSubscribedPayload(user.id))
            }

        // Then unsubscribe from newsletter
        val unsubscribeParams = UpdateUserInfoPostRequest(
            nickname = nickname,
            email = email,
            isNewsLetterSubscriptionAgreed = false,
            mobilityTools = listOf(),
            birthYear = SccRandom.int(1900, 2025),
        )

        mvc
            .sccRequest("/updateUserInfo", unsubscribeParams, userAccount = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                verifyBlocking(stibeeSubscriptionService, atLeastOnce()) { unregisterSubscriber(eq(email)) }
                verify(sccServerEventRecorder, atLeastOnce()).record(NewsletterUnsubscribedPayload(user.id))
            }
    }
}
