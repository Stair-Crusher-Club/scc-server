package club.staircrusher.domain.server_event

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant

class ServerEventTest {

    @Test
    fun `ServerEvent 의 type 과 주어진 payload 의 type 이 일치해야 한다`() {
        val serverEventPayload = NewsletterSubscribedPayload("example")

        assertDoesNotThrow {
            ServerEvent(
                id = "example",
                type = ServerEventType.NEWSLETTER_SUBSCRIBED_ON_SIGN_UP,
                payload = serverEventPayload,
                createdAt = Instant.now(),
            )
        }

        assertThrows<IllegalStateException> {
            ServerEvent(
                id = "example",
                type = ServerEventType.UNKNOWN,
                payload = serverEventPayload,
                createdAt = Instant.now(),
            )
        }
    }
}
