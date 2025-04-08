package club.staircrusher.place.application.port.out.accessibility

interface SlackService {
    fun send(channel: String, content: String)
}
