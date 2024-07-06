package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.time.DayOfWeek

@Component
class GetAccessibilityActivityReportUseCase {
    fun handle(request: Request): Response {
        println("$${request.userId}")
        val todayConqueredCount = 3
        val thisMonthConqueredCount = 37
        val thisWeekConqueredWeekdays = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        return Response(
            todayConqueredCount = todayConqueredCount,
            thisMonthConqueredCount = thisMonthConqueredCount,
            thisWeekConqueredWeekdays = thisWeekConqueredWeekdays
        )
    }

    data class Request(
        val userId: String
    )

    data class Response(
        val todayConqueredCount: Int,
        val thisMonthConqueredCount: Int,
        val thisWeekConqueredWeekdays: List<DayOfWeek>
    )
}
