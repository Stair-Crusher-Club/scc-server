package club.staircrusher.domain.server_event

data class NewsletterSubscribedOnSignupPayload(
    val userId: String,
) : ServerEventPayload {
    override fun getType() = ServerEventType.NEWSLETTER_SUBSCRIBED_ON_SIGN_UP
}
