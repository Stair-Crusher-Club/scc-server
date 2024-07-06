package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.api.spec.dto.DayOfWeek
import club.staircrusher.api.spec.dto.GetAccessibilityActivityReportResponseDto
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityActivityReportController(
    private val accessibilityApplicationService: AccessibilityApplicationService,
) {
    @PostMapping("/getAccessibilityActivityReport")
    fun getAccessibilityActivityReport(
        authentication: SccAppAuthentication?,
    ): GetAccessibilityActivityReportResponseDto {
        return GetAccessibilityActivityReportResponseDto(
            todayConqueredCount = 3,
            thisMonthConqueredCount = 37,
            thisWeekConqueredWeekdays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
        )
    }
}
