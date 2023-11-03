package club.staircrusher.testing.spring_it.mock

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.clock.SccClockBeanHolder
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class MockSccClock private constructor() : SccClock() {
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

    fun reset() {
        instant = Instant.now()
    }

    companion object {
        private lateinit var instance: MockSccClock
        private val monitor = Any()
        fun getInstance(): MockSccClock {
            return synchronized(monitor) {
                if (!::instance.isInitialized) {
                    instance = MockSccClock()
                }
                instance
            }
        }
    }
}
