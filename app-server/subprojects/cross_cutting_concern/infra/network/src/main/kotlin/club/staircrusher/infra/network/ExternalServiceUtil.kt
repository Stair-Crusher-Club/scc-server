package club.staircrusher.infra.network

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.serialization.json.Json
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

val DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10)!!
val DEFAULT_READ_TIMEOUT = Duration.ofSeconds(10)!!
val DEFAULT_WRITE_TIMEOUT = Duration.ofSeconds(10)!!
val DEFAULT_RESPONSE_TIMEOUT = Duration.ofSeconds(2)!!

inline fun <reified T> createExternalApiService(
    baseUrl: String,
    crossinline defaultHeadersBlock: (HttpHeaders) -> Unit,
    crossinline defaultErrorHandler: (ClientResponse) -> Mono<out Throwable>,
    connectionTimeout: Duration = DEFAULT_CONNECT_TIMEOUT,
    readTimeout: Duration = DEFAULT_READ_TIMEOUT,
    writeTimeout: Duration = DEFAULT_WRITE_TIMEOUT,
    responseTimeout: Duration = DEFAULT_RESPONSE_TIMEOUT,
): T {
    val httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout.toMillis().toInt())
        .compress(true)
        .followRedirect(true)
        .doOnConnected {
            it.addHandlerLast(ReadTimeoutHandler(readTimeout.toSeconds(), TimeUnit.SECONDS))
            it.addHandlerLast(WriteTimeoutHandler(writeTimeout.toSeconds(), TimeUnit.SECONDS))
        }
        .responseTimeout(responseTimeout)

    val decoder = KotlinSerializationJsonDecoder(Json { ignoreUnknownKeys = true })

    val client = WebClient.builder()
        .baseUrl(baseUrl)
        .clientConnector(ReactorClientHttpConnector(httpClient))
        .codecs { it.defaultCodecs().kotlinSerializationJsonDecoder(decoder) }
        .defaultHeaders { defaultHeadersBlock(it) }
        .defaultStatusHandler(HttpStatusCode::isError) { response ->
            defaultErrorHandler(response)
        }
        .build()

    val factory = HttpServiceProxyFactory
        .builder(WebClientAdapter.forClient(client))
        .build()
    return factory.createClient(T::class.java)
}
