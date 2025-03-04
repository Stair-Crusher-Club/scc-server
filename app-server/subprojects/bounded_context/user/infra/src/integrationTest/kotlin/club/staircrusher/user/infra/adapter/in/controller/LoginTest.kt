package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.LoginPostRequest
import club.staircrusher.spring_web.security.SccSecurityFilterChainConfig
import club.staircrusher.stdlib.testing.SccRandom
import club.staircrusher.user.infra.adapter.`in`.controller.base.UserITBase
import org.junit.jupiter.api.Test

class LoginTest : UserITBase() {
    @Test
    fun testLogin() {
        val nickname = SccRandom.string(32)
        val password = "password"
        transactionManager.doInTransaction {
            testDataGenerator.createIdentifiedUser(nickname = nickname, password = password)
        }
        val params = LoginPostRequest(
            nickname = nickname,
            password = password,
        )
        mvc
            .sccRequest("/login", params)
            .andExpect {
                header {
                    exists(SccSecurityFilterChainConfig.accessTokenHeader)
                }
            }
    }
}
