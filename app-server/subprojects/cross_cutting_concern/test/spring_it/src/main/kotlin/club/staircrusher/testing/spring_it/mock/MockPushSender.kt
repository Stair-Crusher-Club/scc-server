package club.staircrusher.testing.spring_it.mock

import club.staircrusher.notification.port.`in`.PushService
import club.staircrusher.notification.port.out.PushSender

class MockPushSender: PushSender {
    override suspend fun send(
        pushToken: String,
        customData: Map<String, String>,
        notification: PushService.Notification,
    ): Boolean {
        return true
    }
}
