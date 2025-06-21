package club.staircrusher.slack.application.port.out.web

interface SlackService {
    fun send(channel: String, content: String)
}
