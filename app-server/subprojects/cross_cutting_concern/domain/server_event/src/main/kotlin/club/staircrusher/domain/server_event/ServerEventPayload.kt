package club.staircrusher.domain.server_event

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    JsonSubTypes.Type(NewsletterSubscribedPayload::class),
    JsonSubTypes.Type(NewsletterUnsubscribedPayload::class),
)
sealed interface ServerEventPayload {
    val type: ServerEventType
}

object UnknownServerEventPayload: ServerEventPayload {
    override val type: ServerEventType
        get() = ServerEventType.UNKNOWN
}

data class NewsletterSubscribedPayload(
    val userId: String,
) : ServerEventPayload {
    override val type = ServerEventType.NEWSLETTER_SUBSCRIBED_ON_SIGN_UP
}

data class NewsletterUnsubscribedPayload(
    val userId: String,
) : ServerEventPayload {
    override val type = ServerEventType.NEWSLETTER_UNSUBSCRIBED
}
