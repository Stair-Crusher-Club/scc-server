package club.staircrusher.notification.port.`in`

import club.staircrusher.notification.port.out.PushSender
import club.staircrusher.stdlib.di.annotation.Component

@Component
class PushService(
    private val pushSender: PushSender,
) {
    data class Notification(
        val title: String?,
        val body: String,
        val link: String?,
        val collapseKey: String?
    )

    suspend fun send(
        pushToken: String,
        customData: Map<String, String>,
        notification: Notification,
    ): Boolean {
        return pushSender.send(pushToken, customData, notification)
    }
}
