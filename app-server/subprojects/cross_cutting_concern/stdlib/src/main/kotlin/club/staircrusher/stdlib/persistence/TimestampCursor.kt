package club.staircrusher.stdlib.persistence

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.domain.SccDomainException
import java.time.Instant

open class TimestampCursor(
    val timestamp: Instant,
    // timestamp 가 동일할 경우 사용되는 tiebreaker
    val id: String,
) {
    val value: String = "${timestamp.toEpochMilli()}$DELIMITER$id"

    companion object {
        private const val DELIMITER = "__"

        fun parse(cursorValue: String): TimestampCursor {
            return try {
                val (timestampMillis, id) = cursorValue.split(DELIMITER)
                TimestampCursor(timestamp = Instant.ofEpochMilli(timestampMillis.toLong()), id = id)
            } catch (t: Throwable) {
                throw SccDomainException("Invalid cursor value: $cursorValue", cause = t)
            }
        }

        fun initial() = TimestampCursor(timestamp = SccClock.instant(), id = "")
    }
}
