package club.staircrusher.place.infra.adapter.out.web

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.reactive.awaitSingle
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit


// @Primary
// @Component
class GoogleMapsService : MapsService {
    companion object {
        private val CONNECT_TIMEOUT = Duration.ofSeconds(10)
        private val READ_TIMEOUT = Duration.ofSeconds(10)
        private val WRITE_TIMEOUT = Duration.ofSeconds(10)
    }

    private val logger = KotlinLogging.logger {}

    override suspend fun findAllByKeyword(keyword: String, option: MapsService.SearchByKeywordOption): List<Place> {
        val places = mutableListOf<Place>()
        var result = service.textSearch(
            GooglePlacesApiService.TextSearchParams(
                textQuery = keyword,
            )
        ).awaitSingle()
        logger.info { places.size }
        places.addAll(result.toPlaces())

        while (result.places.isNotEmpty() && result.nextPageToken != null) {
            result = service.textSearch(
                GooglePlacesApiService.TextSearchParams(
                    textQuery = keyword,
                    pageToken = result.nextPageToken,
                )
            ).awaitSingle()
            places.addAll(result.toPlaces())
        }

        logger.info { places.size }
        return places
    }

    override suspend fun findFirstByKeyword(keyword: String, option: MapsService.SearchByKeywordOption): Place? {
        return service.textSearch(
            GooglePlacesApiService.TextSearchParams(
                textQuery = keyword,
            )
        ).awaitSingle().toPlaces().firstOrNull()
    }

    override suspend fun findAllByCategory(
        category: PlaceCategory,
        option: MapsService.SearchByCategoryOption,
    ): List<Place> {
        val places = mutableListOf<Place>()
        val bias = when (option.region) {
            is MapsService.SearchByCategoryOption.CircleRegion -> {
                val region = option.region as MapsService.SearchByCategoryOption.CircleRegion
                GooglePlacesApiService.CircleRestriction(
                    circle = GooglePlacesApiService.Circle(
                        center = GooglePlacesApiService.Point(
                            latitude = region.centerLocation.lat,
                            longitude = region.centerLocation.lng,
                        ),
                        radiusMeters = region.radiusMeters,
                    )
                )
            }
            is MapsService.SearchByCategoryOption.RectangleRegion -> {
                val region = option.region as MapsService.SearchByCategoryOption.RectangleRegion
                GooglePlacesApiService.RectangleRestriction(
                    rectangle = GooglePlacesApiService.Rectangle(
                        low = GooglePlacesApiService.Point(
                            latitude = region.leftBottomLocation.lat,
                            longitude = region.leftBottomLocation.lng,
                        ),
                        high = GooglePlacesApiService.Point(
                            latitude = region.rightTopLocation.lat,
                            longitude = region.rightTopLocation.lng,
                        ),
                    )
                )
            }
        }
        var result = service.textSearch(
            GooglePlacesApiService.TextSearchParams(
                textQuery = category.name,
                locationRestriction = bias,
            )
        ).awaitSingle()
        places.addAll(result.toPlaces())
        logger.info { result }

        while (result.places.isNotEmpty() && result.nextPageToken != null) {
            result = service.textSearch(
                GooglePlacesApiService.TextSearchParams(
                    textQuery = category.name,
                    locationRestriction = bias,
                    pageToken = result.nextPageToken,
                )
            ).awaitSingle()
            logger.info { result }
            places.addAll(result.toPlaces())
        }

        return places
    }

    fun GooglePlacesApiService.TextSearchResult.toPlaces(): List<Place> {
        return places.mapNotNull {
            try {
                Place(
                    id = it.id, // TODO: 제대로 채우기
                    // remove all html tags with regex
                    name = it.displayName.text,
                    location = it.location.let { Location(lng = it.longitude, lat = it.latitude) },
                    building = Building(
                        id = Building.generateId(it.formattedAddress),
                        name = it.formattedAddress,
                        location = it.location.let { Location(lng = it.longitude, lat = it.latitude) },
                        address = it.parseToBuildingAddress(),
                        siGunGuId = "temp", // TODO: 제대로 채우기
                        eupMyeonDongId = "temp",
                    ),
                    siGunGuId = it.formattedAddress,
                    eupMyeonDongId = null,
                    category = null,
                    isClosed = false,
                    isNotAccessible = false,
                )
            } catch (t: Throwable) {
                logger.warn(t) { "Cannot convert document to model: $it" }
                null
            }
        }
    }

    @Suppress("MagicNumber", "VariableNaming")
    fun GooglePlacesApiService.TextSearchResult.Place.parseToBuildingAddress(): BuildingAddress {
        return BuildingAddress(
            siDo = "",
            siGunGu = "",
            eupMyeonDong = "",
            li = "",
            roadName = "",
            mainBuildingNumber = "",
            subBuildingNumber = "",
        )
    }

    private val httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT.toMillis().toInt())
        .compress(true)
        .followRedirect(true)
        .doOnConnected {
            it.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
            it.addHandlerLast(WriteTimeoutHandler(WRITE_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
        }
        .responseTimeout(Duration.ofSeconds(2))
        .wiretap(false)

    private val service: GooglePlacesApiService by lazy {
        val client = WebClient.builder()
            .baseUrl("https://places.googleapis.com/")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs {
                it.defaultCodecs()
                .maxInMemorySize(500 * 1024 * 1024)
            }
            .build()

        val factory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(client))
            .build()
        factory.createClient(GooglePlacesApiService::class.java)
    }

    interface GooglePlacesApiService {
        @PostExchange(
            url = "v1/places:searchText",
            accept = [MediaType.APPLICATION_JSON_VALUE],
        )
        fun textSearch(
            @RequestBody request: TextSearchParams,
            @RequestParam fields: String = "*",
            @RequestParam key: String = ""
        ): Mono<TextSearchResult>

        data class TextSearchParams(
            val textQuery: String,
            val locationRestriction: LocationRestriction? = null,
            val languageCode: String = "ko",
            val pageSize: Int = 20,
            val pageToken: String? = null,
        )

        sealed interface LocationRestriction

        data class CircleRestriction(
            val circle: Circle,
        ) : LocationRestriction

        data class RectangleRestriction(
            val rectangle: Rectangle,
        ) : LocationRestriction

        data class Circle(
            val center: Point,
            val radiusMeters: Int,
        )

        data class Rectangle(
            val low: Point,
            val high: Point,
        )

        data class Point(
            val latitude: Double,
            val longitude: Double,
        )

        data class TextSearchResult(
            val nextPageToken: String? = null,
            val places: List<Place> = emptyList(),
        ) {
            data class Review(
                val authorAttribution: AuthorAttribution,
                val name: String,
                val originalText: OriginalText,
                val publishTime: String,
                val rating: Int,
                val relativePublishTimeDescription: String,
                val text: Text,
            ) {
                data class AuthorAttribution(
                    val displayName: String,
                    val photoUri: String,
                    val uri: String,
                )

                data class OriginalText(
                    val languageCode: String,
                    val text: String,
                )

                data class Text(
                    val languageCode: String,
                    val text: String,
                )
            }

            data class Place(
                val addressComponents: List<AddressComponent>,
                val displayName: DisplayName,
                val formattedAddress: String,
                val id: String,
                val location: Location,
                val name: String,
            ) {
                data class AccessibilityOptions(
                    val wheelchairAccessibleEntrance: Boolean,
                    val wheelchairAccessibleParking: Boolean,
                    val wheelchairAccessibleSeating: Boolean,
                )

                data class AddressComponent(
                    val languageCode: String,
                    val longText: String,
                    val shortText: String,
                    val types: List<String>,
                )

                data class DisplayName(
                    val languageCode: String,
                    val text: String,
                )

                data class Location(
                    val latitude: Double,
                    val longitude: Double,
                )

                data class PrimaryTypeDisplayName(
                    val languageCode: String,
                    val text: String,
                )
            }
        }
    }
}

suspend fun main() {

    val service = GoogleMapsService()
    service.findAllByKeyword("소문난 성수 감자탕", MapsService.SearchByKeywordOption(
        region = MapsService.SearchByKeywordOption.CircleRegion(
            centerLocation = Location( lat=37.54638471729234, lng=127.05375274602797),
            radiusMeters = 300,
        ),
    ))
        .forEach { println(it) }

}
