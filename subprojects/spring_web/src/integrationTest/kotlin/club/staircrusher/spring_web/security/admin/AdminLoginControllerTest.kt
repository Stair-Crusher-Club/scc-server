package club.staircrusher.spring_web.security.admin

import club.staircrusher.admin_api.spec.dto.LoginPostRequest
import club.staircrusher.spring_web.security.SccSecurityFilterChainConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest(properties = ["scc.admin.password=adminPassword"])
@AutoConfigureMockMvc
class AdminLoginControllerTest {
    @Autowired
    private lateinit var mvc: MockMvc

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun test() {
        mvc
            .post("/admin/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsBytes(LoginPostRequest("wrong username", "wrong password"))
                accept = MediaType.APPLICATION_JSON_UTF8
            }
            .andExpect {
                status {
                    isUnauthorized()
                }
            }

        mvc
            .post("/admin/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsBytes(LoginPostRequest(AdminAuthenticationService.ADMIN_USERNAME, "wrong password"))
                accept = MediaType.APPLICATION_JSON_UTF8
            }
            .andExpect {
                status {
                    isUnauthorized()
                }
            }

        mvc
            .post("/admin/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsBytes(LoginPostRequest(AdminAuthenticationService.ADMIN_USERNAME, "adminPassword"))
                accept = MediaType.APPLICATION_JSON_UTF8
            }
            .andExpect {
                status {
                    isNoContent()
                }
                header {
                    exists(SccSecurityFilterChainConfig.accessTokenHeader)
                }
            }
    }
}
