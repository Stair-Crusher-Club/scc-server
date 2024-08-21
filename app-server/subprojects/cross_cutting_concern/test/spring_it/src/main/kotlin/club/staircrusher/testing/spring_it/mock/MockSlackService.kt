package club.staircrusher.testing.spring_it.mock

import club.staircrusher.accessibility.application.port.out.SlackService

class MockSlackService : SlackService {
    override fun send(channel: String, content: String) {
        return
    }
}
