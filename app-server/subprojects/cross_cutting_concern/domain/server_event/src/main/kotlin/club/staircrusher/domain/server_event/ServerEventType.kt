package club.staircrusher.domain.server_event

enum class ServerEventType {
    NEWSLETTER_SUBSCRIBED_ON_SIGN_UP,
    NEWSLETTER_UNSUBSCRIBED,
    // For test
    // TODO: event type 이 더 생기면 테스트 코드 변경하고 삭제하기
    UNKNOWN,
}
