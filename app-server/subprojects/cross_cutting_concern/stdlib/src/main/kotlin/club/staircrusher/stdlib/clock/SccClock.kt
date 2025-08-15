package club.staircrusher.stdlib.clock

import club.staircrusher.stdlib.di.annotation.Component
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@Component
open class SccClock : Clock() {
    init {
        SccClockBeanHolder.setIfNull(this)
    }

    override fun instant(): Instant {
        return Instant.now()
    }

    override fun withZone(zone: ZoneId?): Clock {
        TODO("Not yet implemented")
    }

    override fun getZone(): ZoneId {
        TODO("Not yet implemented")
    }

    companion object {
        fun instant(): Instant {
            val globalSccClock = SccClockBeanHolder.get()
                ?: SccClock().also { SccClockBeanHolder.setIfNull(it) }
            checkNotNull(globalSccClock) { "Cannot use SccClock.instant() since SccClock bean is not initialized yet." }
            return globalSccClock.instant()
        }
    }
}
