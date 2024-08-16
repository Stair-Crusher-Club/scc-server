package club.staircrusher.testing.spring_it.mock

import club.staircrusher.accessibility.application.port.out.NotificationService

class MockNotificationService : NotificationService {
    override fun send(recipient: String, content: String) {
        return
    }
}
