package club.staircrusher.data_restore

import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun readTsvAsLines(resourceName: String): List<List<String>> {
    return object {}.javaClass.classLoader.getResource(resourceName)?.readText()
        ?.split("\n")
        ?.drop(1) // 첫 줄은 header라 버린다.
        ?.map { it.replace("\r", "") }
        ?.map { it.split("\t") }
        ?: emptyList()
}

internal fun writeTextAsFile(text: String, filename: String) {
    File(filename).writeText(text)
}

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val seoulZoneId = ZoneId.of("Asia/Seoul")
internal fun String.toInstant(): Instant {
    return LocalDateTime.parse(this, dateTimeFormatter).atZone(seoulZoneId).toInstant()
}

internal fun Instant.toKstString(): String {
    return this.atZone(seoulZoneId).toLocalDateTime().format(dateTimeFormatter)
}
