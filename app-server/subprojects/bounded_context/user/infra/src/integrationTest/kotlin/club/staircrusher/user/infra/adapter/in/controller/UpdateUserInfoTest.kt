package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.ApiErrorResponse
import club.staircrusher.api.spec.dto.UpdateUserInfoPost200Response
import club.staircrusher.api.spec.dto.UpdateUserInfoPostRequest
import club.staircrusher.api.spec.dto.UserMobilityToolDto
import club.staircrusher.application.server_event.port.`in`.SccServerEventRecorder
import club.staircrusher.domain.server_event.NewsletterSubscribedOnSignupPayload
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
import org.springframework.data.repository.findByIdOrNull

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
            testDataGenerator.createUser()
        }

        val changedNickname = SccRandom.string(32)
        val changedInstagramId = SccRandom.string(32)
        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val changedMobilityTools = listOf(
            UserMobilityTool.ELECTRIC_WHEELCHAIR,
            UserMobilityTool.WALKING_ASSISTANCE_DEVICE,
        )
        assertNotEquals(user.nickname, changedNickname)
        assertNotEquals(user.instagramId, changedInstagramId)
        assertNotEquals(user.email, changedEmail)
        assertNotEquals(user.mobilityTools, changedMobilityTools)

        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
            email = changedEmail,
            mobilityTools = changedMobilityTools.map { it.toDTO() },
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .apply {
                val result = getResult(UpdateUserInfoPost200Response::class)
                assertEquals(user.id, result.user.id)
                assertEquals(changedNickname, result.user.nickname)
                assertEquals(changedInstagramId, result.user.instagramId)
                assertEquals(changedEmail, result.user.email)
                assertEquals(changedMobilityTools.sorted(), result.user.mobilityTools.map { it.toModel() }.sorted())
            }
    }

    @Test
    fun `현재와 같은 데이터로 업데이트가 가능하다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val params = UpdateUserInfoPostRequest(
            nickname = user.nickname,
            instagramId = user.instagramId,
            email = user.email!!,
            mobilityTools = user.mobilityTools.map { it.toDTO() },
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .apply {
                val result = getResult(UpdateUserInfoPost200Response::class)
                assertEquals(user.id, result.user.id)
                assertEquals(user.nickname, result.user.nickname)
                assertEquals(user.instagramId, result.user.instagramId)
                assertEquals(user.email, result.user.email)
            }
    }

    @Test
    fun `중복된 닉네임으로는 변경이 불가능하다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val user2 = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val params = UpdateUserInfoPostRequest(
            nickname = user2.nickname,
            instagramId = user.instagramId,
            email = user.email!!,
            mobilityTools = user.mobilityTools.map { it.toDTO() },
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
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
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val user2 = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val params = UpdateUserInfoPostRequest(
            nickname = user.nickname,
            instagramId = user.instagramId,
            email = user2.email!!,
            mobilityTools = user.mobilityTools.map { it.toDTO() },
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
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
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val params = UpdateUserInfoPostRequest(
            nickname = user.nickname,
            instagramId = user.instagramId,
            email = "strange",
            mobilityTools = user.mobilityTools.map { it.toDTO() },
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
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
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val params = UpdateUserInfoPostRequest(
            nickname = user.nickname,
            instagramId = user.instagramId,
            email = changedEmail,
            mobilityTools = listOf(UserMobilityToolDto.ELECTRIC_WHEELCHAIR, UserMobilityToolDto.PROSTHETIC_FOOT),
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                transactionManager.doInTransaction {
                    val user = userProfileRepository.findById(user.id).get()
                    Assertions.assertEquals(2, user.mobilityTools.size)
                }
            }

        val params2 = UpdateUserInfoPostRequest(
            nickname = user.nickname,
            instagramId = user.instagramId,
            email = changedEmail,
            mobilityTools = listOf(UserMobilityToolDto.MANUAL_WHEELCHAIR),
        )
        mvc
            .sccRequest("/updateUserInfo", params2, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                transactionManager.doInTransaction {
                    val user = userProfileRepository.findById(user.id).get()
                    Assertions.assertEquals(1, user.mobilityTools.size)
                }
            }
    }

    @Test
    fun `뉴스레터 수신에 동의하면 stibee 에 연동한다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val changedNickname = SccRandom.string(32)
        val changedInstagramId = SccRandom.string(32)
        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val changedMobilityTools = listOf(
            UserMobilityTool.ELECTRIC_WHEELCHAIR,
            UserMobilityTool.WALKING_ASSISTANCE_DEVICE,
        )

        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
            email = changedEmail,
            mobilityTools = changedMobilityTools.map { it.toDTO() },
            isNewsLetterSubscriptionAgreed = true,
        )

        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                verifyBlocking(stibeeSubscriptionService, atLeastOnce()) { registerSubscriber(eq(changedEmail), eq(changedNickname), any()) }
                verify(sccServerEventRecorder, atLeastOnce()).record(NewsletterSubscribedOnSignupPayload(user.id))
            }
    }

    @Test
    fun `뉴스레터 수신에 동의하지 않으면 stibee 에 연동하지 않는다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val changedNickname = SccRandom.string(32)
        val changedInstagramId = SccRandom.string(32)
        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val changedMobilityTools = listOf(
            UserMobilityTool.ELECTRIC_WHEELCHAIR,
            UserMobilityTool.WALKING_ASSISTANCE_DEVICE,
        )

        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
            email = changedEmail,
            mobilityTools = changedMobilityTools.map { it.toDTO() },
            isNewsLetterSubscriptionAgreed = false,
        )

        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                verifyBlocking(stibeeSubscriptionService, never()) { registerSubscriber(eq(changedEmail), eq(changedNickname), any()) }
                verify(sccServerEventRecorder, never()).record(NewsletterSubscribedOnSignupPayload(user.id))
            }
    }

    @Test
    fun `명시적인 동의 없이는 stibee 에 연동하지 않는다`() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val changedNickname = SccRandom.string(32)
        val changedInstagramId = SccRandom.string(32)
        val changedEmail = "${SccRandom.string(32)}@staircrusher.club"
        val changedMobilityTools = listOf(
            UserMobilityTool.ELECTRIC_WHEELCHAIR,
            UserMobilityTool.WALKING_ASSISTANCE_DEVICE,
        )

        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
            email = changedEmail,
            mobilityTools = changedMobilityTools.map { it.toDTO() },
            // 하위 호환성
            isNewsLetterSubscriptionAgreed = null,
        )

        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .andExpect {
                status {
                    isOk()
                }
            }
            .apply {
                verifyBlocking(stibeeSubscriptionService, never()) { registerSubscriber(eq(changedEmail), eq(changedNickname), any()) }
                verify(sccServerEventRecorder, never()).record(NewsletterSubscribedOnSignupPayload(user.id))
            }
    }
}
