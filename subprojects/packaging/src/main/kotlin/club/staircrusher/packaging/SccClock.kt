package club.staircrusher.packaging

import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@Component
class SccClock : Clock() {
    override fun instant(): Instant {
        return Instant.now()
    }

    override fun withZone(zone: ZoneId?): Clock {
        TODO("Not yet implemented")
    }

    override fun getZone(): ZoneId {
        TODO("Not yet implemented")
    }
}
