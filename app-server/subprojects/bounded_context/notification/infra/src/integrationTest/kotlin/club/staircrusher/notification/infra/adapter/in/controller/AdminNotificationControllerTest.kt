package club.staircrusher.notification.infra.adapter.`in`.controller

import club.staircrusher.admin_api.converter.toDTO
import club.staircrusher.admin_api.spec.dto.AdminListPushNotificationSchedulesResponseDTO
import club.staircrusher.admin_api.spec.dto.AdminSendPushNotificationRequestDTO
import club.staircrusher.notification.infra.adapter.`in`.controller.base.NotificationITBase
import club.staircrusher.notification.port.`in`.PushScheduleService
import club.staircrusher.notification.port.out.persistence.PushNotificationScheduleRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import java.time.Duration

class AdminNotificationControllerTest : NotificationITBase() {
    @Autowired
    private lateinit var pushNotificationScheduleRepository: PushNotificationScheduleRepository

    @Autowired
    private lateinit var pushScheduleService: PushScheduleService

    @BeforeEach
    fun cleanUp() {
        pushNotificationScheduleRepository.deleteAll()
    }

    @Test
    fun `scheduledAt 필드가 비어 있으면 즉시 전송한다`() {
        // given
        val body = AdminSendPushNotificationRequestDTO(
            scheduledAt = null,
            title = "test title",
            body = "test message",
            deepLink = null,
            userIds = listOf("user1", "user2"),
        )

        // when
        mvc.sccAdminRequest("/admin/notifications/sendPush", HttpMethod.POST, body)
            .andExpect {
                status { isOk() }
            }
            .apply {
                // then
                val schedules = pushNotificationScheduleRepository.findAll().toList()
                Assertions.assertEquals(0, schedules.size)
            }
    }

    @Test
    fun `과거의 시간으로 scheduledAt 을 설정하면 오류가 난다`() {
        // given
        val pastInstant = clock.instant().minusSeconds(60)
        val body = AdminSendPushNotificationRequestDTO(
            scheduledAt = pastInstant.toDTO(),
            title = "test title",
            body = "test message",
            deepLink = null,
            userIds = listOf("user1", "user2"),
        )

        // when
        mvc.sccAdminRequest("/admin/notifications/sendPush", HttpMethod.POST, body)
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `유저의 수가 많으면 청킹해서 스케줄을 생성한다`() {
        // given
        val now = clock.instant().plusSeconds(60)
        val userIds = (1..1010).map { "user$it" }
        val body = AdminSendPushNotificationRequestDTO(
            scheduledAt = now.toDTO(),
            title = "test title",
            body = "test message",
            deepLink = null,
            userIds = userIds,
        )

        mvc.sccAdminRequest("/admin/notifications/sendPush", HttpMethod.POST, body)
            .apply {
                transactionManager.doInTransaction {
                    val schedules = pushNotificationScheduleRepository.findAll().toList()
                    Assertions.assertTrue(schedules.isNotEmpty())
                    Assertions.assertEquals(2, schedules.size)
                    Assertions.assertEquals(schedules[0].groupId, schedules[1].groupId)
                }
            }
    }

    @Test
    fun `청킹해서 스케줄을 생성해도 어드민에서는 한개의 스케쥴로 보인다`() {
        // given
        val scheduledAt = clock.instant().plusSeconds(60)
        val userCount = 1010
        val userIds = (1..userCount).map { "user$it" }
        val body = AdminSendPushNotificationRequestDTO(
            scheduledAt = scheduledAt.toDTO(),
            title = "test title",
            body = "test message",
            deepLink = null,
            userIds = userIds,
        )

        mvc.sccAdminRequest("/admin/notifications/sendPush", HttpMethod.POST, body)
            .andExpect {
                status { isOk() }
            }

        // GET 요청이 너무 빠르게 이뤄지면 cursoring 의 createdAt 조건이 제대로 작동하지 않아 시간을 조금 앞으로 돌린다
        clock.advanceTime(Duration.ofSeconds(5L))

        mvc.sccAdminRequest("/admin/notifications/pushSchedules", HttpMethod.GET, null)
            .andExpect {
                status { isOk() }
            }
            .apply {
                val result = getResult(AdminListPushNotificationSchedulesResponseDTO::class)
                Assertions.assertEquals(1, result.list.size)
                Assertions.assertEquals(userCount, result.list[0].targetUsersCount)
            }
    }
}
