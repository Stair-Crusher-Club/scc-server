package club.staircrusher.accessibility.application.port.out

// TODO: push 기능 생기면 그거랑 합치기
interface NotificationService {
    fun send(recipient: String, content: String)
}
