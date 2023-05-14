package club.staircrusher.user.infra.adapter.`in`.controller

import club.staircrusher.api.spec.dto.SignUpPostRequest
import club.staircrusher.spring_web.security.SccSecurityFilterChainConfig
import club.staircrusher.stdlib.testing.SccRandom
import club.staircrusher.user.infra.adapter.`in`.controller.base.UserITBase
import org.junit.jupiter.api.Test

class SignUpTest : UserITBase() {
    @Test
    fun testSignUp() {
        val nickname = SccRandom.string(32)
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
