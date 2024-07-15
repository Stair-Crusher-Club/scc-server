package club.staircrusher.user.infra.adapter.out.web.login.apple.client

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.KotlinSerializationJsonDecoder
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration(proxyBeanMethods = false)
internal open class AppleLoginApiClientConfiguration {
    @Bean
    open fun appleLoginApiClient(): AppleLoginApiClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT.toMillis().toInt())
            .compress(true)
            .followRedirect(true)
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(READ_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
                it.addHandlerLast(WriteTimeoutHandler(WRITE_TIMEOUT.toSeconds(), TimeUnit.SECONDS))
            }
            .responseTimeout(RESPONSE_TIMEOUT)

        val decoder = KotlinSerializationJsonDecoder(Json { ignoreUnknownKeys = true })

        val client = WebClient.builder()
            .baseUrl("https://appleid.apple.com")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { it.defaultCodecs().kotlinSerializationJsonDecoder(decoder) }
            .defaultStatusHandler(HttpStatusCode::isError) { response ->
                response
                    .bodyToMono(String::class.java)
                    .map { RuntimeException(it) }
                    .onErrorResume { response.createException() }
            }
            .build()

        val factory = HttpServiceProxyFactory
            .builder(WebClientAdapter.forClient(client))
            .build()
        return factory.createClient(AppleLoginApiClient::class.java)
    }

    companion object {
        private val CONNECT_TIMEOUT = Duration.ofSeconds(10)
        private val READ_TIMEOUT = Duration.ofSeconds(10)
        private val WRITE_TIMEOUT = Duration.ofSeconds(10)
        private val RESPONSE_TIMEOUT = Duration.ofSeconds(2)
    }
}
