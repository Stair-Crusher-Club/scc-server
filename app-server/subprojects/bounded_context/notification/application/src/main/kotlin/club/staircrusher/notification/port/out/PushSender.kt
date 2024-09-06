package club.staircrusher.notification.port.out

import club.staircrusher.notification.port.`in`.PushService

interface PushSender {
    suspend fun send(
        pushToken: String,
        customData: Map<String, String>,
        notification: PushService.Notification,
    ): Boolean
}
