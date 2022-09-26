package club.staircrusher.place.infra.adapter.out.web

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.place.PlaceCategory
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
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
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
        ): Mono<SearchResult>

        @GetExchange(
            url = "/v2/local/search/category.json",
            accept = ["application/json"],
        )
        fun searchByCategory(
            @RequestParam(required = false) category_group_code: String? = null,
            @RequestParam(required = false) x: String? = null,
            @RequestParam(required = false) y: String? = null,
            @RequestParam(required = false) radius: Int? = null,
            @RequestParam(required = false) rect: String? = null,
            @RequestParam(required = false) page: Int? = null,
            @RequestParam(required = false) size: Int? = null,
            @RequestParam(required = false) sort: String? = null,
        ): Mono<SearchResult>
    }

    override suspend fun findByKeyword(keyword: String): List<Place> {
        val result = kakaoService.searchByKeyword(keyword).awaitFirstOrNull()
        logger.debug { result }
        return result?.convertToModel() ?: emptyList()
    }

    override suspend fun findByCategory(category: PlaceCategory): List<Place> {
        TODO("Not yet implemented")
    }

    // TODO: 한 번에 한 페이지만 조회하는 대신 동시에 여러 페이지 조회하기
    override suspend fun findAllByCategory(category: PlaceCategory, option: MapsService.SearchOption): List<Place> {
        val result = mutableListOf<Place>()
        var nextPage = 1
        var pageablePage: Int? = null
        while (pageablePage == null || nextPage <= pageablePage) {
            val mono = when (option.region) {
                is MapsService.SearchOption.CircleRegion -> {
                    val region = option.region as MapsService.SearchOption.CircleRegion
                    kakaoService.searchByCategory(
                        category_group_code = SearchResult.Document.Category.fromPlaceCategory(category).name,
                        x = region.centerLocation.lng.toString(),
                        y = region.centerLocation.lat.toString(),
                        radius = region.radiusMeters,
                    )
                }
                is MapsService.SearchOption.RectangleRegion -> {
                    val region = option.region as MapsService.SearchOption.RectangleRegion
                    kakaoService.searchByCategory(
                        category_group_code = SearchResult.Document.Category.fromPlaceCategory(category).name,
                        rect = region.let { "${it.leftTopLocation.lng},${it.leftTopLocation.lat},${it.rightBottomLocation.lng},${it.rightBottomLocation.lat}" }
                    )
                }
            }
            val apiResult = mono.awaitFirstOrNull()
            logger.debug { apiResult }
            if (apiResult == null) {
                break
            }
            pageablePage = apiResult.meta.pageableCount
            nextPage += 1
            result += apiResult.convertToModel()
        }
        return result
    }

    private fun SearchResult.convertToModel(): List<Place> {
        return documents.map {
            Place(
                id = it.id,
                name = it.placeName,
                location = Location(
                    lng = it.x.toDouble(),
                    lat = it.y.toDouble(),
                ),
                building = null,
                siGunGuId = null,
                eupMyeonDongId = null,
                category = it.categoryGroupCode.toPlaceCategory(),
            )
        }
    }

    @Serializable
    data class SearchResult(
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
            val categoryGroupCode: Category,
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
        ) {
            @Serializable
            enum class Category {
                MT1, //	대형마트
                CS2, //	편의점
                PS3, //	어린이집, 유치원
                SC4, //	학교
                AC5, //	학원
                PK6, //	주차장
                OL7, //	주유소, 충전소
                SW8, //	지하철역
                BK9, //	은행
                CT1, //	문화시설
                AG2, //	중개업소
                PO3, //	공공기관
                AT4, //	관광명소
                AD5, //	숙박
                FD6, //	음식점
                CE7, //	카페
                HP8, //	병원
                PM9, //	약국
                ;

                fun toPlaceCategory(): PlaceCategory {
                    return when (this) {
                        MT1 -> PlaceCategory.MARKET
                        CS2 -> PlaceCategory.CONVENIENCE_STORE
                        PS3 -> PlaceCategory.KINDERGARTEN
                        SC4 -> PlaceCategory.SCHOOL
                        AC5 -> PlaceCategory.ACADEMY
                        PK6 -> PlaceCategory.PARKING_LOT
                        OL7 -> PlaceCategory.GAS_STATION
                        SW8 -> PlaceCategory.SUBWAY_STATION
                        BK9 -> PlaceCategory.BANK
                        CT1 -> PlaceCategory.CULTURAL_FACILITIES
                        AG2 -> PlaceCategory.AGENCY
                        PO3 -> PlaceCategory.PUBLIC_OFFICE
                        AT4 -> PlaceCategory.ATTRACTION
                        AD5 -> PlaceCategory.ACCOMODATION
                        FD6 -> PlaceCategory.RESTAURANT
                        CE7 -> PlaceCategory.CAFE
                        HP8 -> PlaceCategory.HOSPITAL
                        PM9 -> PlaceCategory.PHARMACY
                    }
                }

                companion object {
                    fun fromPlaceCategory(placeCategory: PlaceCategory): Category {
                        return when (placeCategory) {
                            PlaceCategory.MARKET -> MT1
                            PlaceCategory.CONVENIENCE_STORE -> CS2
                            PlaceCategory.KINDERGARTEN -> PS3
                            PlaceCategory.SCHOOL -> SC4
                            PlaceCategory.ACADEMY -> AC5
                            PlaceCategory.PARKING_LOT -> PK6
                            PlaceCategory.GAS_STATION -> OL7
                            PlaceCategory.SUBWAY_STATION -> SW8
                            PlaceCategory.BANK -> BK9
                            PlaceCategory.CULTURAL_FACILITIES -> CT1
                            PlaceCategory.AGENCY -> AG2
                            PlaceCategory.PUBLIC_OFFICE -> PO3
                            PlaceCategory.ATTRACTION -> AT4
                            PlaceCategory.ACCOMODATION -> AD5
                            PlaceCategory.RESTAURANT -> FD6
                            PlaceCategory.CAFE -> CE7
                            PlaceCategory.HOSPITAL -> HP8
                            PlaceCategory.PHARMACY -> PM9
                        }
                    }
                }
            }
        }

        @Serializable
        data class Meta(
            @SerialName("is_end")
            val isEnd: Boolean,
            @SerialName("pageable_count")
            val pageableCount: Int,
            @SerialName("same_name")
            val sameName: RegionInfo?,
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
