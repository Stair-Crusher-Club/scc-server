package club.staircrusher.place.infra.adapter.out.web

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Building
import club.staircrusher.place.domain.model.BuildingAddress
import club.staircrusher.place.domain.model.Place
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.geography.Location
import club.staircrusher.stdlib.place.PlaceCategory
import com.google.common.util.concurrent.RateLimiter
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import jakarta.annotation.Priority
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
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
@Priority(Int.MAX_VALUE)
class NaverMapsService(
    private val naverOpenApiProperties: NaverOpenApiProperties,
): MapsService {
    companion object {
        private val CONNECT_TIMEOUT = Duration.ofSeconds(10)
        private val READ_TIMEOUT = Duration.ofSeconds(10)
        private val WRITE_TIMEOUT = Duration.ofSeconds(10)

        private val logger = KotlinLogging.logger {}
    }

    @Suppress("UnstableApiUsage", "MagicNumber")
    private val rateLimiter = RateLimiter.create(1.0)

    private val httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT.toMillis().toInt())
        .compress(true)
        .followRedirect(true)
        .doOnConnected {
            it.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
            it.addHandlerLast(WriteTimeoutHandler(WRITE_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
        }
        .responseTimeout(Duration.ofSeconds(2))
    private val decoder = KotlinSerializationJsonDecoder(Json { ignoreUnknownKeys = true })

    private val naverOpenApiService: NaverOpenApiService by lazy {
        val client = WebClient.builder()
            .baseUrl("https://openapi.naver.com")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { it.defaultCodecs().kotlinSerializationJsonDecoder(decoder) }
            .defaultHeaders {
                it.add("X-Naver-Client-Id", naverOpenApiProperties.clientId)
                it.add("X-Naver-Client-Secret", naverOpenApiProperties.clientSecret)
            }
            .defaultStatusHandler(HttpStatusCode::isError) { response ->
                response
                    .bodyToMono(NaverOpenApiError::class.java)
                    .map { RuntimeException(it.errorMessage) }
                    .onErrorResume { response.createException() }
            }
            .build()

        val factory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(client))
            .build()
        factory.createClient(NaverOpenApiService::class.java)
    }

    @Suppress("FunctionParameterNaming")
    interface NaverOpenApiService {
        @GetExchange(
            url = "v1/search/local.json",
            accept = [MediaType.APPLICATION_JSON_VALUE],
        )
        fun localSearch(
            @RequestParam query: String,
            @RequestParam(required = false) display: Int? = 5,
            @RequestParam(required = false) start: Int? = 1,
            @RequestParam(required = false) sort: String? = "random",
        ): Mono<LocalSearchResult>

        companion object {
            internal const val defaultSize = 5
        }
    }

    override suspend fun findAllByKeyword(
        keyword: String,
        option: MapsService.SearchByKeywordOption,
    ): List<Place> {
        throw NotImplementedError()
    }

    override suspend fun findFirstByKeyword(
        keyword: String,
        option: MapsService.SearchByKeywordOption,
    ): Place? {
        return fetchPageForSearchByKeyword(keyword)
            .convertToModel()
            .firstOrNull()
    }

    override suspend fun findAllByCategory(
        category: PlaceCategory,
        option: MapsService.SearchByCategoryOption,
    ): List<Place> {
        throw NotImplementedError()
    }

    private suspend fun fetchPageForSearchByKeyword(
        keyword: String,
    ): LocalSearchResult {
        // FIXME: because rate limiter implementation of guice blocks current thread until it can acquire permit,
        // and we are using kotlin coroutine or project reactor which are using size-limited thread pool normally,
        // it could block all threads and prevent them from running.
        @Suppress("UnstableApiUsage")
        rateLimiter.acquire()

        return naverOpenApiService.localSearch(
            query = keyword,
        ).awaitFirst()
    }

    @Serializable
    data class LocalSearchResult(
        val lastBuildDate: String,
        val total: Int,
        val start: Int,
        val display: Int,
        val items: List<LocalSearchItem>,
    ) {
        @Serializable
        data class LocalSearchItem(
            val title: String,
            val link: String,
            val category: String,
            val description: String?,
            val telephone: String?,
            val address: String,
            val roadAddress: String,
            val mapx: String,
            val mapy: String,
        ) {
            @Suppress("MagicNumber")
            val location: Location
                get() = Location(mapx.toDouble() / 1e7, mapy.toDouble() / 1e7)

            @Suppress("MagicNumber", "VariableNaming")
            fun parseToBuildingAddress(): BuildingAddress {
                val addressNameTokens = address.split(" ")
                val siDo = addressNameTokens[0]
                val siGunGu = addressNameTokens[1]
                val eupMyeonDong = addressNameTokens[2]
                val li = addressNameTokens.getOrNull(3)?.takeIf { it.endsWith("리") } ?: ""

                /**
                 * 네이버는 도로명주소에 상세주소(e.g., 106동 803호)]와 상호명(e.g., 문화식당)이 같이 나오는 경우가 있으므로
                 * 건물번호를 찾아내기 위해서 건물번호 형식이 나올 때 까지 drop 한다.
                 *
                 * https://www.juso.go.kr/CommonPageLink.do?link=/street/GuideBook
                 * 도로명주소 공식 가이드북에 따르면 건물 번호의 경우 아래와 같이 부여한다.
                 *
                 * 1. 건물번호 e.g., 102
                 * 2. 건물번호 + 부번 e.g., 23-2
                 * 3. 지하의 사용 e.g., 지하11
                 */
                val 건물번호_정규식 = """(지하)?\d+(-\d+)?""".toRegex()
                val (roadName, buildingNumber) = this.roadAddress
                    .split(" ")
                    .dropLastWhile { !건물번호_정규식.matches(it) }
                    .takeLast(2)
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
        }

        fun convertToModel(): List<Place> {
            return items.mapNotNull {
                @Suppress("TooGenericExceptionCaught", "SwallowedException")
                try {
                    Place(
                        id = "temp", // TODO: 제대로 채우기
                        // remove all html tags with regex
                        name = it.title.replace("<[^>]*>".toRegex(), ""),
                        location = it.location,
                        building = Building(
                            id = Building.generateId(it.roadAddress),
                            name = it.roadAddress,
                            location = it.location,
                            address = it.parseToBuildingAddress(),
                            siGunGuId = "temp", // TODO: 제대로 채우기
                            eupMyeonDongId = "temp",
                        ),
                        siGunGuId = null,
                        eupMyeonDongId = null,
                        category = null,
                    )
                } catch (t: Throwable) {
                    logger.warn(t) { "Cannot convert document to model: $it" }
                    null
                }
            }
        }
    }

    @Serializable
    data class NaverOpenApiError(
        val errorMessage: String,
        val errorCode: String,
    )
}
