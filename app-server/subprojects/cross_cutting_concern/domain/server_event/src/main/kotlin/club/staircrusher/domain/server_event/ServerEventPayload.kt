package club.staircrusher.domain.server_event

interface ServerEventPayload {
    // getType 으로 이름을 붙이면 jackson 에서 getter 로 인식하면서 함께 직렬화가 된다
    fun type(): ServerEventType
}
