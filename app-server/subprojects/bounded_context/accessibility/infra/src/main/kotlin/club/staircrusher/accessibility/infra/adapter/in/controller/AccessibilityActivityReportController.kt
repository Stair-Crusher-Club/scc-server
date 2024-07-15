package club.staircrusher.accessibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.GetAccessibilityActivityReportUseCase
import club.staircrusher.api.spec.dto.DayOfWeek
import club.staircrusher.api.spec.dto.GetAccessibilityActivityReportResponseDto
import club.staircrusher.spring_web.security.app.SccAppAuthentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AccessibilityActivityReportController(
    private val getAccessibilityActivityReportUseCase: GetAccessibilityActivityReportUseCase
) {
    @PostMapping("/getAccessibilityActivityReport")
    fun getAccessibilityActivityReport(
        authentication: SccAppAuthentication?,
    ): GetAccessibilityActivityReportResponseDto {
        val userId = authentication?.details?.id ?: throw IllegalArgumentException("인증되지 않는 유저입니다.")
        val response =
            getAccessibilityActivityReportUseCase.handle(GetAccessibilityActivityReportUseCase.Request(userId))
        return GetAccessibilityActivityReportResponseDto(
            todayConqueredCount = response.todayConqueredCount,
            thisMonthConqueredCount = response.thisMonthConqueredCount,
            thisWeekConqueredWeekdays = response.thisWeekConqueredWeekdays
                .map {
                    when (it) {
                        java.time.DayOfWeek.SUNDAY -> DayOfWeek.SUNDAY
                        java.time.DayOfWeek.MONDAY -> DayOfWeek.MONDAY
                        java.time.DayOfWeek.TUESDAY -> DayOfWeek.TUESDAY
                        java.time.DayOfWeek.WEDNESDAY -> DayOfWeek.WEDNESDAY
                        java.time.DayOfWeek.THURSDAY -> DayOfWeek.THURSDAY
                        java.time.DayOfWeek.FRIDAY -> DayOfWeek.FRIDAY
                        java.time.DayOfWeek.SATURDAY -> DayOfWeek.SATURDAY
                    }
                }
        )
    }
}
