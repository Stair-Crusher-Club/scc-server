package club.staircrusher.quest.application.port.out.web

import java.time.Duration

interface UrlShorteningService {
    fun shorten(
        url: String,
        expiryDuration: Duration? = null,
    ): String
}
