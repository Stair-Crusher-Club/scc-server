package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.UpdateUserInfoPost200Response
import club.staircrusher.api.spec.dto.UpdateUserInfoPostRequest
import club.staircrusher.user.infra.adapter.`in`.controller.base.UserITBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class UpdateUserInfoTest : UserITBase() {
    @Test
    fun updateUserInfoTest() {
        val user = transactionManager.doInTransaction {
            testDataGenerator.createUser()
        }

        val changedNickname = UUID.randomUUID().toString().take(32)
        val changedInstagramId = UUID.randomUUID().toString().take(32)
        val changedEmail = UUID.randomUUID().toString().take(32)
        assertNotEquals(user.nickname, changedNickname)
        assertNotEquals(user.instagramId, changedInstagramId)
        assertNotEquals(user.email, changedEmail)

        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
            email = changedEmail
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .apply {
                val result = getResult(UpdateUserInfoPost200Response::class)
                assertEquals(user.id, result.user.id)
                assertEquals(changedNickname, result.user.nickname)
                assertEquals(changedInstagramId, result.user.instagramId)
                assertEquals(changedEmail, result.user.email)
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
            email = user.email!!
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .andExpect {
                status {
                    isBadRequest()
                }
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
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .andExpect {
                status {
                    isBadRequest()
                }
            }
    }
}
