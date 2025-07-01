package club.staircrusher.notification.infra.adapter.`in`.controller

import club.staircrusher.notification.port.`in`.SendScheduledPushNotificationsUseCase
import club.staircrusher.spring_web.security.InternalIpAddressChecker
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class NotificationController(
    private val sendScheduledPushNotificationsUseCase: SendScheduledPushNotificationsUseCase,
) {
    @PostMapping("/sendScheduledPushNotifications")
    fun sendScheduledPushNotifications(request: HttpServletRequest) {
        InternalIpAddressChecker.check(request)

        sendScheduledPushNotificationsUseCase.handle()
    }
}
