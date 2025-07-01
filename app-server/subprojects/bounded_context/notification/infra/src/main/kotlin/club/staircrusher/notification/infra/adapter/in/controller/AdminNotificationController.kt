package club.staircrusher.notification.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminListPushNotificationSchedulesResponseDTO
import club.staircrusher.admin_api.spec.dto.AdminPushNotificationScheduleDTO
import club.staircrusher.admin_api.spec.dto.AdminSendPushNotificationRequestDTO
import club.staircrusher.admin_api.spec.dto.AdminUpdatePushNotificationScheduleRequestDTO
import club.staircrusher.notification.port.`in`.PushScheduleService
import club.staircrusher.notification.port.`in`.SendOrSchedulePushNotificationUseCase
import club.staircrusher.spring_web.security.admin.SccAdminAuthentication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class AdminNotificationController(
    private val sendOrSchedulePushNotificationUseCase: SendOrSchedulePushNotificationUseCase,
    private val pushScheduleService: PushScheduleService
) {
    @PostMapping("/admin/notifications/sendPush")
    fun adminSendPushNotification(
        @RequestBody request: AdminSendPushNotificationRequestDTO,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) {
        sendOrSchedulePushNotificationUseCase.handle(
            scheduledAt = request.scheduledAt?.let { Instant.ofEpochMilli(it.value) },
            targetUserIds = request.userIds,
            title = request.title,
            body = request.body,
            deepLink = request.deepLink,
        )
    }

    @GetMapping("/admin/notifications/pushSchedules")
    fun adminListPushNotificationSchedules(
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) cursor: String?,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ): AdminListPushNotificationSchedulesResponseDTO {
        val (items, nextCursor) = pushScheduleService.list(limit, cursor)
        return AdminListPushNotificationSchedulesResponseDTO(
            list = items.map { it.toAdminDTO() },
            cursor = nextCursor,
        )
    }

    @GetMapping("/admin/notifications/pushSchedules/{scheduleGroupId}")
    fun adminGetPushNotificationSchedule(
        @PathVariable scheduleGroupId: String,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) : AdminPushNotificationScheduleDTO {
        return pushScheduleService.get(scheduleGroupId).toAdminDTO()
    }

    @PutMapping("/admin/notifications/pushSchedules/{scheduleGroupId}")
    fun updatePushNotificationSchedule(
        @PathVariable scheduleGroupId: String,
        @RequestBody request: AdminUpdatePushNotificationScheduleRequestDTO,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) : ResponseEntity<Unit> {
        pushScheduleService.update(
            groupId = scheduleGroupId,
            title = request.title,
            body = request.body,
            deepLink = request.deepLink,
            scheduledAt = Instant.ofEpochMilli(request.scheduledAt.value),
        )

        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/admin/notifications/pushSchedules/{scheduleGroupId}")
    fun deletePushNotificationSchedule(
        @PathVariable scheduleGroupId: String,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) : ResponseEntity<Unit> {
        pushScheduleService.delete(scheduleGroupId)
        return ResponseEntity.noContent().build()
    }
}
