package club.staircrusher

import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.place.infra.adapter.out.web.KakaoMapsService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun readTsvAsLines(resourceName: String): List<List<String>> {
    return object {}.javaClass.classLoader.getResource(resourceName)?.readText()
        ?.split("\n")
        ?.drop(1) // 첫 줄은 header라 버린다.
        ?.filter { it.isNotBlank() }
        ?.map { it.replace("\r", "") }
        ?.map { it.split("\t") }
        ?: emptyList()
}

internal fun readCsvAsLines(resourceName: String): List<List<String>> {
    return object {}.javaClass.classLoader.getResource(resourceName)?.readText()
        ?.split("\n")
        ?.drop(1) // 첫 줄은 header라 버린다.
        ?.filter { it.isNotBlank() }
        ?.map { it.replace("\r", "") }
        ?.map { it.split(",") } // FIXME: 데이터 안에 ,가 있을 수 있다.
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

data class FindLngLatResult(
    val placeName: String,
    val place: Place,
    val lng: Double,
    val lat: Double,
)
internal fun KakaoMapsService.findLngLat(placeNames: List<String>): List<FindLngLatResult> {
    return runBlocking {
        val deferredList = placeNames.map { placeName ->
            async {
                val place = findFirstByKeyword(placeName, MapsService.SearchByKeywordOption())
                    ?: throw IllegalArgumentException("${placeName}에 해당하는 장소가 없습니다.")
                println("station found: $placeName / ${place.name}")
                FindLngLatResult(
                    placeName,
                    place,
                    place.location.lng,
                    place.location.lat,
                )
            }
        }
        awaitAll(*deferredList.toTypedArray())
    }
}
