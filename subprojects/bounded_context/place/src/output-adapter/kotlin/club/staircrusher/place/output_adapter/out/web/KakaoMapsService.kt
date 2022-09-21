package club.staircrusher.place.output_adapter.out.web

import club.staircrusher.place.application.port.out.web.MapsService
import club.staircrusher.place.domain.model.Place
import club.staircrusher.place.domain.model.PlaceCategory
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.client.reactive.ReactorResourceFactory
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.GetExchange
import reactor.core.publisher.Mono
import reactor.netty.http.HttpResources
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
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

        val client = WebClient.builder()
            .baseUrl("https://dapi.kakao.com")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader("Authorization", "KakaoAK $kakaoProperties")
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
        ): Mono<String>
    }


    override suspend fun findByAddress(address: String): List<Place> {
        TODO("Not yet implemented")
    }

    override suspend fun findByKeyword(keyword: String): List<Place> {
        val result = kakaoService.searchByKeyword(keyword).awaitFirstOrNull()
        logger.info { result }
        return listOf()
    }

    override suspend fun findByCategory(category: PlaceCategory): List<Place> {
        TODO("Not yet implemented")
    }
}