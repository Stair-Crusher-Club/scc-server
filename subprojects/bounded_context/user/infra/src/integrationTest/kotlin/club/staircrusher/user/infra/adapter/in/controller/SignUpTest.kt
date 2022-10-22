package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.SignUpPostRequest
import club.staircrusher.spring_web.security.SccSecurityFilterChainConfig
import club.staircrusher.user.infra.adapter.`in`.controller.base.UserITBase
import org.junit.jupiter.api.Test
import kotlin.random.Random

class SignUpTest : UserITBase() {
    @Test
    fun testSignUp() {
        val nickname = Random.nextBytes(32).toString()
        val params = SignUpPostRequest(
            nickname = nickname,
            instagramId = "instagramId",
            password = "password",
        )
        mvc
            .sccRequest("/signUp", params)
            .andExpect {
                header {
                    exists(SccSecurityFilterChainConfig.accessTokenHeader)
                }
            }
    }
}
