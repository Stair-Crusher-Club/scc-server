package club.staircrusher.api.converter

import club.staircrusher.api.spec.dto.EpochMillisTimestamp
import java.time.Instant

fun Instant.toDTO() = EpochMillisTimestamp(value = toEpochMilli())
