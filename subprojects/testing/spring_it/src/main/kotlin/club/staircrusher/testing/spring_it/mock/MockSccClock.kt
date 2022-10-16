package club.staircrusher.testing.spring_it.mock

import club.staircrusher.stdlib.di.annotation.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@Component
class MockSccClock : Clock() {
    private var zone: ZoneId
    private var instant: Instant

    init {
        val clock = systemUTC()
        zone = clock.zone
        instant = clock.instant()
    }

    override fun getZone(): ZoneId {
        return zone
    }

    override fun withZone(zone: ZoneId): Clock {
        this.zone = zone
        return this
    }

    override fun instant(): Instant {
        return instant
    }

    fun advanceTime(duration: Duration) {
        instant += duration
    }
}
