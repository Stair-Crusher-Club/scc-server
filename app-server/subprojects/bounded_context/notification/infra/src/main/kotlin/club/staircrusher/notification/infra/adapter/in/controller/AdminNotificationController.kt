package club.staircrusher.notification.infra.adapter.`in`.controller

import club.staircrusher.admin_api.spec.dto.AdminSendPushNotificationRequestDto
import club.staircrusher.notification.port.`in`.PushScheduleService
import club.staircrusher.notification.port.`in`.SendOrSchedulePushNotificationUseCase
import club.staircrusher.spring_web.security.admin.SccAdminAuthentication
import club.staircrusher.stdlib.clock.SccClock
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminNotificationController(
    private val sendOrSchedulePushNotificationUseCase: SendOrSchedulePushNotificationUseCase,
    private val pushScheduleService: PushScheduleService
) {
    @PostMapping("/admin/notifications/sendPush")
    fun adminSendPushNotification(
        @RequestBody request: AdminSendPushNotificationRequestDto,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) {
        sendOrSchedulePushNotificationUseCase.handle(
            scheduledAt = null,
            targetUserIds = request.userIds,
            title = request.notification.title,
            body = request.notification.body,
            deepLink = request.notification.deepLink,
        )
    }

    @GetMapping("/admin/notifications/pushSchedule")
    fun adminListPushNotificationSchedules(
        @RequestParam(required = false) limit: Int?,
        @RequestParam(required = false) cursor: String?,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) {
        val (items, nextCursor) = pushScheduleService.list(limit, cursor)
        return
    }

    @GetMapping("/admin/notifications/pushSchedule/{scheduleId}")
    fun adminGetPushNotificationSchedule(
        @PathVariable scheduleId: String,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) {
        pushScheduleService.get(scheduleId)
    }

    @PutMapping("/admin/notifications/pushSchedule/{scheduleId}")
    fun updatePushNotificationSchedule(
        @PathVariable scheduleId: String,
        @RequestBody request: AdminSendPushNotificationRequestDto,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) {
        // TODO: fix using api-spec
        pushScheduleService.update(
            scheduleId,
            userIds = request.userIds,
            title = request.notification.title,
            body = request.notification.body,
            deepLink = request.notification.deepLink,
            scheduledAt = SccClock.instant()
        )
    }

    @DeleteMapping("/admin/notifications/pushSchedule/{scheduleId}")
    fun deletePushNotificationSchedule(
        @PathVariable scheduleId: String,
        @Suppress("UnusedPrivateMember") authentication: SccAdminAuthentication,
    ) {
        return pushScheduleService.delete(scheduleId)
    }
}
