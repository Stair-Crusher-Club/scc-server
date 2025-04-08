package club.staircrusher.testing.spring_it.mock

import club.staircrusher.place.application.port.out.accessibility.SlackService


class MockSlackService : SlackService {
    override fun send(channel: String, content: String) {
        return
    }
}
