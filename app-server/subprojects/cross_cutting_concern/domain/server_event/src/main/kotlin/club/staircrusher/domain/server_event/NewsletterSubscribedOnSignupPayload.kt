package club.staircrusher.domain.server_event

data class NewsletterSubscribedOnSignupPayload(
    val userId: String,
) : ServerEventPayload {
    override val type = ServerEventType.NEWSLETTER_SUBSCRIBED_ON_SIGN_UP
}
