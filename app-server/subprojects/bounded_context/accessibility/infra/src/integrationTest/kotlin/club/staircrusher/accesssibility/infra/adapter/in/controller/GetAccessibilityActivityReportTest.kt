package club.staircrusher.accesssibility.infra.adapter.`in`.controller

import club.staircrusher.accessibility.application.port.`in`.AccessibilityApplicationService
import club.staircrusher.accessibility.application.port.out.persistence.BuildingAccessibilityRepository
import club.staircrusher.accessibility.application.port.out.persistence.PlaceAccessibilityRepository
import club.staircrusher.api.spec.dto.DayOfWeek
import club.staircrusher.api.spec.dto.GetAccessibilityActivityReportResponseDto
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.time.getDayOfMonth
import club.staircrusher.stdlib.time.toEndOfMonth
import club.staircrusher.stdlib.time.toStartOfMonth
import club.staircrusher.stdlib.time.toStartOfWeek
import club.staircrusher.testing.spring_it.base.SccSpringITBase
import club.staircrusher.testing.spring_it.mock.MockSccClock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.temporal.ChronoUnit

class GetAccessibilityActivityReportTest : SccSpringITBase() {
    @Autowired
    lateinit var clock: MockSccClock

    @Autowired
    private lateinit var accessibilityApplicationService: AccessibilityApplicationService

    @Autowired
    private lateinit var placeAccessibilityRepository: PlaceAccessibilityRepository

    @Autowired
    private lateinit var buildingAccessibilityRepository: BuildingAccessibilityRepository


    @BeforeEach
    fun setUp() = transactionManager.doInTransaction {
    }

    @Test
    fun `오늘, 이번달, 이번주 정복량을 내려준다`() {
        val now = SccClock.instant()
        val startDayOfThisMonth = now.toStartOfMonth()
        val endDayOfThisMonth = now.toEndOfMonth()
        val daysOfThisMonth = endDayOfThisMonth.getDayOfMonth() - startDayOfThisMonth.getDayOfMonth()
        val user = transactionManager.doInTransaction { testDataGenerator.createUser() }
        val accessibilities = transactionManager.doInTransaction {
            // 매일 12시에 1개씩 등록
            return@doInTransaction (0 until daysOfThisMonth).map { index ->
                val createdAt = startDayOfThisMonth.plus(12L + index * 24L, ChronoUnit.HOURS)
                val place = testDataGenerator.createBuildingAndPlace()
                val placeAccessibility =
                    testDataGenerator.registerPlaceAccessibility(place = place, user = user, at = createdAt)
                val buildingAccessibility =
                    testDataGenerator.registerBuildingAccessibilityIfNotExists(
                        building = place.building,
                        user = user,
                        at = createdAt
                    )
                return@map placeAccessibility to buildingAccessibility
            }
        }
        val result = mvc
            .sccRequest("/getAccessibilityActivityReport", null, user = user)
            .getResult(GetAccessibilityActivityReportResponseDto::class)
        val daysOfThisWeek = now.getDayOfMonth() - now.toStartOfWeek().getDayOfMonth() + 1
        Assertions.assertEquals(result.todayConqueredCount, 2)
        Assertions.assertEquals(result.thisMonthConqueredCount, daysOfThisMonth * 2)
        Assertions.assertEquals(
            result.thisWeekConqueredWeekdays,
            (1..daysOfThisWeek).map { idx -> java.time.DayOfWeek.of(idx) }.map { DayOfWeek.valueOf(it.name) }
        )
    }
}
