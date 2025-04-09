package club.staircrusher.spring_web.web

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class SccExceptionHandlerTest {
    @Autowired
    lateinit var mvc: MockMvc

    @Test
    fun `SccDomainException 테스트`() {
        mvc
            .get("/throwSccDomainException")
            .andExpect {
                status {
                    isBadRequest()
                }
                content {
                    json("""{"msg":"SccDomainException thrown.","code":null}""")
                }
            }
    }
}
