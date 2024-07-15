package club.staircrusher.domain.server_log

data class NewsletterSubscribedOnSignupPayload(
    val userId: String,
) : ServerLogPayload {
    override val type = ServerLogType.NEWSLETTER_SUBSCRIBED_ON_SIGN_UP
}
