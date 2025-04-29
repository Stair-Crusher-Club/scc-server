package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.ReportAccessibilityPostRequest
import club.staircrusher.place.application.port.out.accessibility.SlackService
import club.staircrusher.place.application.port.out.accessibility.persistence.AccessibilityReportRepository
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean

class ReportAccessibilityTest : AccessibilityITBase() {
    @MockBean
    lateinit var slackService: SlackService

    @Autowired
    lateinit var accessibilityReportRepository: AccessibilityReportRepository

    @Test
    fun `신고하기를 누를 경우 메세지가 전송된다`() {
        val accessibilityResult = registerAccessibility()
        val place = accessibilityResult.place
        val user = accessibilityResult.user

        val params =
            ReportAccessibilityPostRequest(placeId = place.id, reason = "INACCURATE_INFO")
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

    @Test
    fun `신고하기를 누를 경우 신고 내용이 저장된다`() {
        val accessibilityResult = registerAccessibility()
        val place = accessibilityResult.place
        val user = accessibilityResult.user

        val reason = "BAD_USER"
        val detail = "상세 내용"
        val params = ReportAccessibilityPostRequest(placeId = place.id, reason = reason, detail = detail)

        mvc
            .sccRequest("/reportAccessibility", params, user)
            .andExpect {
                status { isNoContent() }
            }

        // Verify that the report was saved to the database
        val reports = accessibilityReportRepository.findByPlaceId(place.id)
        assertEquals(1, reports.size)

        val report = reports.first()
        assertNotNull(report.id)
        assertEquals(place.id, report.placeId)
        assertEquals(user.id, report.userId)
        assertEquals(
            club.staircrusher.place.domain.model.accessibility.AccessibilityReportReason.BadUser, report.reason
        )
        assertEquals(detail, report.detail)
    }
}
