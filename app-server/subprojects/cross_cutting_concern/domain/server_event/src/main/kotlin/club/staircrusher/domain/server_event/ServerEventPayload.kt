package club.staircrusher.domain.server_event

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    JsonSubTypes.Type(NewsletterSubscribedOnSignupPayload::class)
)
sealed interface ServerEventPayload {
    val type: ServerEventType
}

object UnknownServerEventPayload: ServerEventPayload {
    override val type: ServerEventType
        get() = ServerEventType.UNKNOWN
}

data class NewsletterSubscribedOnSignupPayload(
    val userId: String,
) : ServerEventPayload {
    override val type = ServerEventType.NEWSLETTER_SUBSCRIBED_ON_SIGN_UP
}
