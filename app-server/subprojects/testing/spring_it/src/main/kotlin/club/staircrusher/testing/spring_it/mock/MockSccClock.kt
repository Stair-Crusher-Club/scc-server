package club.staircrusher.testing.spring_it.mock

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.clock.SccClockBeanHolder
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.context.annotation.Primary
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

@Component
@Primary
class MockSccClock : SccClock() {
    private var zone: ZoneId
    private var instant: Instant

    init {
        val clock = systemUTC()
        zone = clock.zone
        instant = clock.instant()
        SccClockBeanHolder.set(this)
    }

    override fun getZone(): ZoneId {
        return zone
    }

    override fun instant(): Instant {
        return instant
    }

    fun advanceTime(duration: Duration) {
        instant += duration
    }
}
