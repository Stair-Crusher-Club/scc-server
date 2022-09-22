package club.staircrusher.place.output_adapter.out.web

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.place.domain.model.PlaceCategory
import club.staircrusher.stdlib.geography.Location
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit


class KakaoMapsService(
    kakaoProperties: Any,
): MapsService {
    private val logger = KotlinLogging.logger {}
    private val kakaoService: KakaoService by lazy {
        // FIXME: extract configurable parameters to kakaoProperties
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .compress(true)
            .followRedirect(true)
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(10, TimeUnit.SECONDS))
                it.addHandlerLast(WriteTimeoutHandler(10, TimeUnit.SECONDS))
            }
            .responseTimeout(Duration.ofSeconds(2))

        val decoder = KotlinSerializationJsonDecoder(Json { ignoreUnknownKeys = true })

        val client = WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { it.defaultCodecs().kotlinSerializationJsonDecoder(decoder) }
            .defaultHeaders { it.add(HttpHeaders.AUTHORIZATION, "KakaoAK $kakaoProperties") }
            .defaultStatusHandler(HttpStatusCode::isError) { response ->
                response
                    .bodyToMono(KakaoError::class.java)
                    .map { RuntimeException(it.msg) }
                    .onErrorResume { response.createException() }
            }
            .build()

        val factory = WebClientAdapter.createHttpServiceProxyFactory(client)
        factory.afterPropertiesSet()
        factory.createClient(KakaoService::class.java)
    }

    interface KakaoService {
        @GetExchange(
            url = "/v2/local/search/keyword.json",
            accept = ["application/json"],
        )
        fun searchByKeyword(
            @RequestParam query: String,
            @RequestParam(required = false) category_group_code: String? = null,
            @RequestParam(required = false) x: String? = null,
            @RequestParam(required = false) y: String? = null,
            @RequestParam(required = false) radius: Int? = null,
            @RequestParam(required = false) rect: String? = null,
            @RequestParam(required = false) page: Int? = null,
            @RequestParam(required = false) size: Int? = null,
            @RequestParam(required = false) sort: String? = null,
        ): Mono<KeywordSearchResult>
    }

    override suspend fun findByKeyword(keyword: String): List<Place> {
        val result = kakaoService.searchByKeyword(keyword).awaitFirstOrNull()
        logger.debug { result }
        return result?.documents?.map {
            Place(
                id = it.id,
                name = it.placeName,
                location = Location(
                    lng = it.x.toDouble(),
                    lat = it.y.toDouble(),
                ),
                building = TODO(),
                siGunGuId = TODO(),
                eupMyeonDongId = TODO(),
                category = TODO()// it.categoryGroupCode,
            )
        } ?: emptyList()
    }

    override suspend fun findByCategory(category: PlaceCategory): List<Place> {
        TODO("Not yet implemented")
    }

    @Serializable
    data class KeywordSearchResult(
        @SerialName("documents")
        val documents: List<Document>,
        @SerialName("meta")
        val meta: Meta,
    ) {
        @Serializable
        data class Document(
            @SerialName("address_name")
            val addressName: String,
            @SerialName("category_group_code")
            val categoryGroupCode: String,
            @SerialName("category_group_name")
            val categoryGroupName: String,
            @SerialName("category_name")
            val categoryName: String,
            @SerialName("distance")
            val distance: String,
            @SerialName("id")
            val id: String,
            @SerialName("phone")
            val phone: String,
            @SerialName("place_name")
            val placeName: String,
            @SerialName("place_url")
            val placeUrl: String,
            @SerialName("road_address_name")
            val roadAddressName: String,
            @SerialName("x")
            val x: String,
            @SerialName("y")
            val y: String,
        )

        @Serializable
        data class Meta(
            @SerialName("is_end")
            val isEnd: Boolean,
            @SerialName("pageable_count")
            val pageableCount: Int,
            @SerialName("same_name")
            val sameName: RegionInfo,
            @SerialName("total_count")
            val totalCount: Int,
        ) {
            @Serializable
            data class RegionInfo(
                @SerialName("keyword")
                val keyword: String,
                @SerialName("region")
                val region: List<String>,
                @SerialName("selected_region")
                val selectedRegion: String,
            )
        }
    }

    @Serializable
    data class KakaoError(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
    )
}