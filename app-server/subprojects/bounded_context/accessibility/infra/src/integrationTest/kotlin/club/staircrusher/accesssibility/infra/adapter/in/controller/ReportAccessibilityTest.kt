package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.out.SlackService
import club.staircrusher.accesssibility.infra.adapter.`in`.controller.base.AccessibilityITBase
import club.staircrusher.api.spec.dto.ReportAccessibilityPostRequest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.MockBean

class ReportAccessibilityTest : AccessibilityITBase() {
    @MockBean
    lateinit var slackService: SlackService

    @Test
    fun `신고하기를 누를 경우 메세지가 전송된다`() {
        val accessibilityResult = registerAccessibility()
        val place = accessibilityResult.place
        val user = accessibilityResult.user

        val params = ReportAccessibilityPostRequest(placeId = place.id, reason = "아이유")
        mvc
            .sccRequest("/reportAccessibility", params, user)
            .andExpect {
                status { isNoContent() }
            }
            .apply {
                verify(slackService, times(1)).send(
                    channel = eq("#scc-accessibility-report-test"),
                    any(),
                )
            }
    }
}
