package club.staircrusher.quest.infra.adapter.out.service

import club.staircrusher.infra.network.createExternalApiService
import club.staircrusher.quest.application.port.out.web.UrlShorteningFailureException
import club.staircrusher.quest.application.port.out.web.UrlShorteningService
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class NhnCloudUrlShorteningService(
    private val nhnCloudUrlShorteningProperties: NhnCloudUrlShorteningProperties,
) : UrlShorteningService {
    private val nhnCloudService = createExternalApiService<NhnCloudService>(
        baseUrl = "https://api-shorturl.nhncloudservice.com",
        defaultHeadersBlock = {},
        defaultErrorHandler = { response ->
            response
                .bodyToMono(String::class.java)
                .map { RuntimeException(it) }
                .onErrorResume { response.createException() }
        },
    )

    override fun shorten(
        url: String,
        expiryDuration: Duration?,
    ): String {
        val endDateTime = expiryDuration
            ?.let { SccClock.instant() + it }
            ?.atZone(ZoneId.of("Asia/Seoul"))
            ?.let { DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(it) }
        val response = nhnCloudService.createShortenedUrl(
            appKey = nhnCloudUrlShorteningProperties.appKey,
            requestBody = NhnCloudService.CreateShortenedUrlRequest(
                url = url,
                endDateTime = endDateTime,
            ),
        )

        if (!response.header.isSuccessful) {
            throw UrlShorteningFailureException("Failed to shorten url: $url, expiryDuration: $expiryDuration; response: $response")
        }
        return response.body!!.shortUrl
    }

    /**
     * https://docs.nhncloud.com/ko/Application%20Service/ShortURL/ko/api-guide/
     */
    private interface NhnCloudService {
        @PostExchange(
            url = "/open-api/v1.0/appkeys/{appKey}/urls",
            accept = ["application/json"],
            contentType = "application/json"
        )
        fun createShortenedUrl(
            @PathVariable appKey: String,
            @RequestBody requestBody: CreateShortenedUrlRequest,
        ): CreateShortenedUrlResponse

        data class CreateShortenedUrlRequest(
            val url: String,
            val endDateTime: String?,
        )

        data class CreateShortenedUrlResponse(
            val header: Header,
            val body: Body?,
        ) {
            data class Header(
                val isSuccessful: Boolean,
                val resultCode: Int,
                val resultMessage: String,
            )

            data class Body(
                val shortUrl: String,
                val originUrl: String,
                val status: String,
                val backHalfType: String,
                val description: String?,
                val startDateTime: String?,
                val endDateTime: String?,
            )
        }
    }
}
