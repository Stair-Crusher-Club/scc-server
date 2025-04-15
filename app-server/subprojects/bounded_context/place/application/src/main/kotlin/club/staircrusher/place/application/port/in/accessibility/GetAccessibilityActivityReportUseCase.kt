package club.staircrusher.place.application.port.`in`.accessibility

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.time.getDayOfWeek
import club.staircrusher.stdlib.time.toStartOfDay
import club.staircrusher.stdlib.time.toStartOfMonth
import club.staircrusher.stdlib.time.toStartOfWeek
import java.time.DayOfWeek

@Component
class GetAccessibilityActivityReportUseCase(
    private val accessibilityApplicationService: AccessibilityApplicationService
) {
    fun handle(request: Request): Response {
        val now = SccClock.instant()
        val todayConqueredCount = accessibilityApplicationService.countByUserIdAndCreatedAtBetween(
            userId = request.userId, from = now.toStartOfDay(), to = now
        )
        val thisMonthConqueredCount = accessibilityApplicationService.countByUserIdAndCreatedAtBetween(
            userId = request.userId, from = now.toStartOfMonth(), to = now
        )
        val (placeAccessibilities, buildingAccessibilities) = accessibilityApplicationService.findByUserIdAndCreatedAtBetween(
            userId = request.userId, from = now.toStartOfWeek(), to = now
        )
        val createdAts = placeAccessibilities.map { it.createdAt }.plus(buildingAccessibilities.map { it.createdAt })
        return Response(
            todayConqueredCount = todayConqueredCount,
            thisMonthConqueredCount = thisMonthConqueredCount,
            thisWeekConqueredWeekdays = createdAts.map { it.getDayOfWeek() }.toSortedSet().toList(),
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
