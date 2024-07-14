package club.staircrusher.testing.spring_it.mock

import club.staircrusher.quest.application.port.out.web.UrlShorteningService
import java.time.Duration

class MockUrlShorteningService : UrlShorteningService {
    override fun shorten(url: String, expiryDuration: Duration?): String {
        return url
    }
}
