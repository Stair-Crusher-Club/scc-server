package club.staircrusher.notification.infra.adapter.`in`.controller

import club.staircrusher.notification.infra.adapter.`in`.controller.base.NotificationITBase
import club.staircrusher.notification.port.`in`.PushScheduleService
import club.staircrusher.notification.port.out.persistence.PushNotificationScheduleRepository
import org.junit.jupiter.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod

class AdminNotificationControllerTest : NotificationITBase() {
    @Autowired
    private lateinit var pushNotificationScheduleRepository: PushNotificationScheduleRepository

    @Autowired
    private lateinit var pushScheduleService: PushScheduleService

    // TODO: API spec 머지 후 테스트 코드 완성
    fun `scheduledAt 필드가 비어 있으면 즉시 전송한다`() {
        // given
        val now = clock.instant()
        val params = mapOf(
            "scheduledAt" to null,
            "title" to "test title",
            "body" to "test message",
            "deepLink" to null,
            "targetUserIds" to listOf("user1", "user2"),
        )

        // then
        val schedules = pushNotificationScheduleRepository.findAll().toList()
        Assertions.assertEquals(1, schedules.size)
        Assertions.assertEquals("test message", schedules[0].body)
        Assertions.assertTrue(schedules[0].isSent())
    }

    fun `유저의 수가 많으면 청킹해서 스케줄을 생성한다`() {
        // given
        val now = clock.instant()
        val userIds = (1..1010).map { "user$it" }
        val requestBody = mapOf(
            "scheduledAt" to now.toString(),
            "title" to "test title",
            "body" to "test message",
            "deepLink" to null,
            "targetUserIds" to userIds,
        )

        mvc.sccAdminRequest("/admin/notifications/sendPush", HttpMethod.POST, requestBody)
            .apply {
                transactionManager.doInTransaction {
                    val schedules = pushNotificationScheduleRepository.findAll().toList()
                    Assertions.assertTrue(schedules.isNotEmpty())
                    Assertions.assertEquals(2, schedules.size)
                    Assertions.assertTrue(schedules[1].userIds.contains("user1010"))
                }
            }
    }
}
