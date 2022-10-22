package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.LoginPostRequest
import club.staircrusher.spring_web.security.SccSecurityFilterChainConfig
import club.staircrusher.user.infra.adapter.`in`.controller.base.UserITBase
import org.junit.jupiter.api.Test
import kotlin.random.Random

class LoginTest : UserITBase() {
    @Test
    fun testLogin() {
        val nickname = Random.nextBytes(32).toString()
        val password = "password"
        transactionManager.doInTransaction {
            testDataGenerator.createUser(nickname = nickname, password = password)
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
