package club.staircrusher.accessibility.application.port.`in`

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.time.toEndOfDay
import club.staircrusher.stdlib.time.toEndOfMonth
import club.staircrusher.stdlib.time.toStartOfDay
import club.staircrusher.stdlib.time.toStartOfMonth
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

@Component
class GetAccessibilityActivityReportUseCase(
    private val accessibilityApplicationService: AccessibilityApplicationService
) {
    fun handle(request: Request): Response {
        val now = SccClock.instant()

        now.atZone(ZoneId.of("Asia/Seoul")).toLocalDate().atStartOfDay()
        val todayConqueredCount = accessibilityApplicationService.countByUserIdAndBetween(
            userId = request.userId, from = now.toStartOfDay(), to = now.toEndOfDay()
        )
        val thisMonthConqueredCount = accessibilityApplicationService.countByUserIdAndBetween(
            userId = request.userId, from = now.toStartOfMonth(), to = now.toEndOfMonth()
        )
        val (placeAccessibilities, buildingAccessibilities) = accessibilityApplicationService.findByUserIdAndBetween(
            userId = request.userId, from = now.toStartOfDay(), to = now.toEndOfDay()
        )
        val hasActivityOnDayOfWeek = checkWhetherActivityExistsOnDayOfWeek(placeAccessibilities.map { it.createdAt }
            .plus(buildingAccessibilities.map { it.createdAt }))
            .mapIndexed { index, hasActivity ->
                hasActivity to DayOfWeek.of(index + 1)
            }
            .mapNotNull { (hasActivity, dayOfWeek) -> if (hasActivity) dayOfWeek else null }
        return Response(
            todayConqueredCount = todayConqueredCount,
            thisMonthConqueredCount = thisMonthConqueredCount,
            thisWeekConqueredWeekdays = hasActivityOnDayOfWeek,
        )
    }

    private fun checkWhetherActivityExistsOnDayOfWeek(
        accessibilityCreatedAts: List<Instant>,
        zoneId: ZoneId = ZoneId.of("Asia/Seoul")
    ): BooleanArray {
        val weekdays = BooleanArray(7) { false }
        accessibilityCreatedAts.forEach { createdAt ->
            val dayOfWeek = createdAt.atZone(zoneId).dayOfWeek.value
            weekdays[(dayOfWeek - 1) % 7] = true
        }
        return weekdays
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
