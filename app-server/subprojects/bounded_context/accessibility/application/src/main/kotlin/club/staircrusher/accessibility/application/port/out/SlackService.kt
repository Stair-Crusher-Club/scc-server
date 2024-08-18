package club.staircrusher.accessibility.application.port.out

interface SlackService {
    fun send(channel: String, content: String)
}
