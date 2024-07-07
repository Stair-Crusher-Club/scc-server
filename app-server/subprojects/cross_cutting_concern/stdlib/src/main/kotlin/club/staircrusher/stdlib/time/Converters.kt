package club.staircrusher.stdlib.time

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

fun Instant.toOffsetDateTime() = atOffset(ZoneOffset.UTC)

fun Instant.toStartOfDay(zoneId: ZoneId = ZoneId.of("Asia/Seoul")): Instant =
    atZone(zoneId).truncatedTo(ChronoUnit.DAYS).toInstant()

fun Instant.toEndOfDay(zoneId: ZoneId = ZoneId.of("Asia/Seoul")): Instant =
    atZone(zoneId).with(LocalTime.MAX).toInstant()

fun Instant.toStartOfMonth(zoneId: ZoneId = ZoneId.of("Asia/Seoul")): Instant =
    atZone(zoneId)
        .with(TemporalAdjusters.firstDayOfMonth())
        .truncatedTo(ChronoUnit.DAYS)
        .toInstant()

fun Instant.toEndOfMonth(zoneId: ZoneId = ZoneId.of("Asia/Seoul")): Instant =
    atZone(zoneId)
        .with(TemporalAdjusters.lastDayOfMonth())
        .with(LocalTime.MAX)
        .toInstant()

fun Instant.toStartOfWeek(zoneId: ZoneId = ZoneId.of("Asia/Seoul")): Instant =
    atZone(zoneId)
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .truncatedTo(ChronoUnit.DAYS)
        .toInstant()

fun Instant.toEndOfWeek(zoneId: ZoneId = ZoneId.of("Asia/Seoul")): Instant =
    toStartOfWeek(zoneId)
        .plus(6, ChronoUnit.DAYS)
        .toEndOfDay(zoneId)

fun Instant.getDayOfMonth(zoneId: ZoneId = ZoneId.of("Asia/Seoul")): Int =
    atZone(zoneId).dayOfMonth
