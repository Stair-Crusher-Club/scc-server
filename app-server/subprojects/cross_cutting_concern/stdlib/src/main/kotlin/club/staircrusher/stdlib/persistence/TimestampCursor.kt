package club.staircrusher.stdlib.persistence

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.SccDomainException
import java.time.Instant

open class TimestampCursor(
    val id: String,
    val timestamp: Instant,
) {
    val value: String = "$id$DELIMITER${timestamp.toEpochMilli()}"

    companion object {
        private const val DELIMITER = "__"

        fun parse(cursorValue: String): TimestampCursor {
            return try {
                val (id, timestampMillis) = cursorValue.split(DELIMITER)
                TimestampCursor(id = id, timestamp = Instant.ofEpochMilli(timestampMillis.toLong()))
            } catch (t: Throwable) {
                throw SccDomainException("Invalid cursor value: $cursorValue", cause = t)
            }
        }

        fun initial() = TimestampCursor(id = "", timestamp = SccClock.instant())
    }
}
