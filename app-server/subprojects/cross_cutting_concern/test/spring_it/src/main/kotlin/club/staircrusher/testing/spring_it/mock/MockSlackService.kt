package club.staircrusher.testing.spring_it.mock

import club.staircrusher.slack.application.port.out.web.SlackService


class MockSlackService : SlackService {
    override fun send(channel: String, content: String) {
        return
    }
}
