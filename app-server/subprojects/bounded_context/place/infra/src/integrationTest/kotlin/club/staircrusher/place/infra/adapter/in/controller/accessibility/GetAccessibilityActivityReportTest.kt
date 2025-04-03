package club.staircrusher.place.infra.adapter.`in`.controller.accessibility

import club.staircrusher.api.spec.dto.DayOfWeek
import club.staircrusher.api.spec.dto.GetAccessibilityActivityReportResponseDto
import club.staircrusher.place.infra.adapter.`in`.controller.accessibility.base.AccessibilityITBase
import club.staircrusher.stdlib.time.getDayOfMonth
import club.staircrusher.stdlib.time.toStartOfWeek
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZonedDateTime

class GetAccessibilityActivityReportTest : AccessibilityITBase() {

    @Test
    fun `오늘, 이번달, 이번주 정복량을 내려준다`() {
        val baseLocalDateTime = LocalDateTime.of(2024, 7, 1, 12, 0)

        val userAccount = transactionManager.doInTransaction { testDataGenerator.createIdentifiedUser().account }
        (0 until 10).map { index ->
            transactionManager.doInTransaction {
                val createdAt = ZonedDateTime.of(baseLocalDateTime.plusDays(index.toLong()), clock.zone).toInstant()
                val place = testDataGenerator.createBuildingAndPlace()
                testDataGenerator.registerPlaceAccessibility(place = place, userAccount = userAccount, at = createdAt)
                testDataGenerator.registerBuildingAccessibilityIfNotExists(
                    building = place.building,
                    userAccount = userAccount,
                    at = createdAt
                )
            }
        }

        val now = ZonedDateTime.of(baseLocalDateTime.plusHours(1L), clock.zone).toInstant()
        clock.setTime(now)

        val result = mvc
            .sccRequest("/getAccessibilityActivityReport", null, userAccount = userAccount)
            .getResult(GetAccessibilityActivityReportResponseDto::class)

        val daysOfThisWeek = now.getDayOfMonth() - now.toStartOfWeek().getDayOfMonth() + 1
        val daysOfThisMonthUntilToday = now.getDayOfMonth()
        Assertions.assertEquals(result.todayConqueredCount, 2)
        Assertions.assertEquals(result.thisMonthConqueredCount, daysOfThisMonthUntilToday * 2)
        Assertions.assertEquals(
            result.thisWeekConqueredWeekdays,
            (1..daysOfThisWeek).map { idx -> java.time.DayOfWeek.of(idx) }.map { DayOfWeek.valueOf(it.name) }
        )
    }
}
