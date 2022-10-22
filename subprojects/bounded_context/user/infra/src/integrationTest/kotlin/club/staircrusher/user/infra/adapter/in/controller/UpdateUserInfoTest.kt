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
        assertNotEquals(user.nickname, changedNickname)
        assertNotEquals(user.instagramId, changedInstagramId)

        val params = UpdateUserInfoPostRequest(
            nickname = changedNickname,
            instagramId = changedInstagramId,
        )
        mvc
            .sccRequest("/updateUserInfo", params, user = user)
            .apply {
                val result = getResult(UpdateUserInfoPost200Response::class)
                assertEquals(user.id, result.user.id)
                assertEquals(changedNickname, result.user.nickname)
                assertEquals(changedInstagramId, result.user.instagramId)
            }
    }
}
