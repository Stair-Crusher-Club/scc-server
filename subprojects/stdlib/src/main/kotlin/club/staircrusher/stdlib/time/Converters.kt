package club.staircrusher.stdlib.time

import java.time.Instant
import java.time.ZoneOffset

fun Instant.toOffsetDateTime() = atOffset(ZoneOffset.UTC)
