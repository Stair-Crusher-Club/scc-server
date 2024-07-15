package club.staircrusher.admin_api.converter

import club.staircrusher.admin_api.spec.dto.EpochMillisTimestamp
import java.time.Instant

fun Instant.toDTO() = EpochMillisTimestamp(value = toEpochMilli())
