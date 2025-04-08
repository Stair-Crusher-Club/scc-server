package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.ReportAccessibilityPostRequest
import club.staircrusher.place.application.port.out.accessibility.SlackService
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
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
