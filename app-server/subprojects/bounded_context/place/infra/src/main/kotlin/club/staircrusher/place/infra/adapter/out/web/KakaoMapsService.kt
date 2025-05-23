package club.staircrusher.place.infra.adapter.out.web

import club.staircrusher.infra.network.RateLimiterFactory
import club.staircrusher.place.application.port.out.place.web.MapsService
import club.staircrusher.place.domain.model.place.Building
import club.staircrusher.place.domain.model.place.BuildingAddress
import club.staircrusher.place.domain.model.place.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import jakarta.annotation.Priority
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
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
@Priority(Int.MAX_VALUE - 1)
class KakaoMapsService(
    kakaoMapsProperties: KakaoMapsProperties,
    rateLimiterFactory: RateLimiterFactory,
) : MapsService {
    private val logger = KotlinLogging.logger {}
    private val rateLimiter = rateLimiterFactory.create("kakao_maps", REQUEST_PER_SECOND_LIMIT)

    private val kakaoService: KakaoService by lazy {
        // FIXME: extract configurable parameters to kakaoMapsProperties
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT.toMillis().toInt())
            .compress(true)
            .followRedirect(true)
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
                it.addHandlerLast(WriteTimeoutHandler(WRITE_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
            }
            .responseTimeout(Duration.ofSeconds(2))

        val decoder = KotlinSerializationJsonDecoder(Json { ignoreUnknownKeys = true })

        val client = WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { it.defaultCodecs().kotlinSerializationJsonDecoder(decoder) }
            .defaultHeaders { it.add(HttpHeaders.AUTHORIZATION, "KakaoAK ${kakaoMapsProperties.restApiKey}") }
            .defaultStatusHandler(HttpStatusCode::isError) { response ->
                response
                    .bodyToMono(KakaoError::class.java)
                    .map { RuntimeException(it.msg) }
                    .onErrorResume { response.createException() }
            }
            .build()

        val factory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(client))
            .build()
        factory.createClient(KakaoService::class.java)
    }

    // https://developers.kakao.com/docs/latest/ko/local/dev-guide
    @Suppress("FunctionParameterNaming")
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
            @RequestParam(required = true) category_group_code: String,
            @RequestParam(required = false) x: String? = null,
            @RequestParam(required = false) y: String? = null,
            @RequestParam(required = false) radius: Int? = null,
            @RequestParam(required = false) rect: String? = null,
            @RequestParam(required = false) page: Int? = null,
            @RequestParam(required = false) size: Int? = null,
            @RequestParam(required = false) sort: String? = null,
        ): Mono<SearchResult>

        companion object {
            internal const val defaultSize = 15
        }
    }

    override suspend fun findAllByKeyword(keyword: String, option: MapsService.SearchByKeywordOption): List<Place> {
        return searchPlacesInParallel { page -> fetchPageForSearchByKeyword(keyword, option.region, SearchByKeywordOption(page = page)) }
    }

    override suspend fun findFirstByKeyword(keyword: String, option: MapsService.SearchByKeywordOption): Place? {
        return fetchPageForSearchByKeyword(keyword, option.region, SearchByKeywordOption(page = 1))
            .map { it.convertToModel() }
            .awaitFirstOrNull()
            ?.firstOrNull()
    }

    data class SearchByKeywordOption(
        val page: Int? = null,
        val size: Int = DEFAULT_SIZE,
    ) {
        companion object {
            const val MAX_SIZE = 45
            const val DEFAULT_SIZE = 15
        }
    }

    data class SearchByCategoryOption(
        val page: Int? = null,
        val size: Int = MAX_SIZE,
    ) {
        companion object {
            const val MAX_SIZE = 15
        }
    }

    private fun fetchPageForSearchByKeyword(
        keyword: String,
        region: MapsService.SearchByKeywordOption.Region? = null,
        option: SearchByKeywordOption
    ): Mono<SearchResult> {
        return if (rateLimiter.tryConsume(1)) {
            when (region) {
                is MapsService.SearchByKeywordOption.CircleRegion -> {
                    kakaoService.searchByKeyword(
                        query = keyword,
                        x = region.centerLocation.lng.toString(),
                        y = region.centerLocation.lat.toString(),
                        radius = region.radiusMeters,
                        page = option.page,
                        sort = region.sort.value
                    )
                }
                is MapsService.SearchByKeywordOption.RectangleRegion -> {
                    kakaoService.searchByKeyword(
                        query = keyword,
                        rect = region.let { "${it.leftTopLocation.lng},${it.leftTopLocation.lat},${it.rightBottomLocation.lng},${it.rightBottomLocation.lat}" },
                        page = option.page,
                    )
                }
                null -> kakaoService.searchByKeyword(query = keyword, page = option.page)
            }
        } else {
            // FIXME: if it exceeds the rate limit, it simply returns null
            //   It might be better to throw an exception showing an error message to the user
            Mono.empty()
        }
    }

    override suspend fun findAllByCategory(category: PlaceCategory, option: MapsService.SearchByCategoryOption): List<Place> {
        return searchPlacesInParallel { page -> fetchPageForSearchByCategory(category, option.region, SearchByCategoryOption(page = page)) }
    }

    private fun fetchPageForSearchByCategory(
        category: PlaceCategory,
        region: MapsService.SearchByCategoryOption.Region,
        option: SearchByCategoryOption
    ): Mono<SearchResult> {
        return if (rateLimiter.tryConsume(1)) {
            when (region) {
                is MapsService.SearchByCategoryOption.CircleRegion -> {
                    kakaoService.searchByCategory(
                        category_group_code = SearchResult.Document.Category.fromPlaceCategory(category).name,
                        x = region.centerLocation.lng.toString(),
                        y = region.centerLocation.lat.toString(),
                        radius = region.radiusMeters,
                        page = option.page,
                        sort = region.sort.value
                    )
                }
                is MapsService.SearchByCategoryOption.RectangleRegion -> {
                    kakaoService.searchByCategory(
                        category_group_code = SearchResult.Document.Category.fromPlaceCategory(category).name,
                        rect = region.let { "${it.leftBottomLocation.lng},${it.leftBottomLocation.lat},${it.rightTopLocation.lng},${it.rightTopLocation.lat}" },
                        page = option.page,
                    )
                }
            }
        } else {
            Mono.empty()
        }
    }

    @Suppress("ReturnCount")
    private suspend fun searchPlacesInParallel(fetchPageBlock: (page: Int) -> Mono<SearchResult>): List<Place> {
        val firstPageResult = fetchPageBlock(1)
            .awaitFirstOrNull()
            ?: return emptyList()
        // 첫 요청한 갯수로 page 나누기
        val pageablePage = maxOf(0, firstPageResult.meta.pageableCount - 1) / KakaoService.defaultSize + 1 // TODO: KakaoService.defaultSize를 고정적으로 사용하지 말고, searchOption에 넣는 size를 사용해서 페이지 계산하기

        val result = firstPageResult.convertToModel().toMutableList()
        if (pageablePage == 1) {
            return result
        }

        val chunkedResult = (2..pageablePage).map { fetchPageBlock(it) }
            .chunked(FETCH_CHUNK_SIZE)

        chunkedResult.forEach { chunkedMonos ->
            val searchedPlaces = Mono.zip(chunkedMonos) {
                it.flatMap { (it as SearchResult).convertToModel() }
            }.awaitFirstOrNull()

            searchedPlaces?.let { result += it }
        }

        return result.removeDuplicates()
    }

    private fun SearchResult.convertToModel(): List<Place> {
        return documents
            .filterNot { it.isNonPlaceResult() }
            .mapNotNull {
                @Suppress("TooGenericExceptionCaught", "SwallowedException")
                try {
                    Place.of(
                        id = it.id,
                        name = it.placeName,
                        location = it.location,
                        building = Building(
                            id = Building.generateId(it.roadAddressName),
                            name = it.roadAddressName,
                            location = it.location,
                            address = it.parseToBuildingAddress(),
                            siGunGuId = "temp", // TODO: 제대로 채우기
                            eupMyeonDongId = "temp",
                        ),
                        siGunGuId = null,
                        eupMyeonDongId = null,
                        category = it.categoryGroupCode?.toPlaceCategory(),
                        isClosed = false,
                        isNotAccessible = false,
                    )
                } catch (t: Throwable) {
                    logger.warn(t) { "Cannot convert document to model: $it" }
                    null
                }
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
            private val rawCategoryGroupCode: String,
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
            val location: Location
                get() = Location(lng = x.toDouble(), lat = y.toDouble())

            @Suppress("SwallowedException")
            val categoryGroupCode: Category?
                get() = try {
                    Category.valueOf(rawCategoryGroupCode)
                } catch (_: IllegalArgumentException) {
                    null
                }

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

                @Suppress("ComplexMethod")
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
                    @Suppress("ComplexMethod")
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

            /**
             * 일부 검색 결과는 점포가 아닌 경우가 있다. 해당 경우들은 검색 결과로 보여주지 않아야 한다.
             * - case 1. "운중천" - roadAddressName이 empty string으로 내려온다.
             */
            fun isNonPlaceResult(): Boolean {
                return roadAddressName.isBlank()
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

    @Suppress("MagicNumber")
    fun SearchResult.Document.parseToBuildingAddress(): BuildingAddress {
        val addressNameTokens = addressName.split(" ")
        val siDo = addressNameTokens[0]
        val siGunGu = addressNameTokens[1]
        val eupMyeonDong = addressNameTokens[2]
        val li = addressNameTokens.getOrNull(3)?.takeIf { it.endsWith("리") } ?: ""

        val (roadName, buildingNumber) = roadAddressName.split(" ").takeLast(2)
        val buildingNumberTokens = buildingNumber.split("-")
        val (mainBuildingNumber, subBuildingNumber) = if (buildingNumberTokens.size == 1) {
            Pair(buildingNumberTokens[0], "")
        } else {
            Pair(buildingNumberTokens[0], buildingNumberTokens[1])
        }
        return BuildingAddress(
            siDo = siDo,
            siGunGu = siGunGu,
            eupMyeonDong = eupMyeonDong,
            li = li,
            roadName = roadName,
            mainBuildingNumber = mainBuildingNumber,
            subBuildingNumber = subBuildingNumber,
        )
    }

    @Serializable
    data class KakaoError(
        @SerialName("code")
        val code: Int,
        @SerialName("msg")
        val msg: String,
    )

    private fun List<Place>.removeDuplicates(): List<Place> {
        return associateBy { it.id }.values.toList()
    }

    companion object {
        private val CONNECT_TIMEOUT = Duration.ofSeconds(10)
        private val READ_TIMEOUT = Duration.ofSeconds(10)
        private val WRITE_TIMEOUT = Duration.ofSeconds(10)

        private const val FETCH_CHUNK_SIZE = 20
        private const val REQUEST_PER_SECOND_LIMIT = 20L
    }
}
