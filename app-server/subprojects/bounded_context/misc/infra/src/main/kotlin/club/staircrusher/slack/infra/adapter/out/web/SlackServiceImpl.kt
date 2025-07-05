package club.staircrusher.slack.infra.adapter.out.web

import club.staircrusher.infra.network.createExternalApiService
import club.staircrusher.slack.application.port.out.web.SlackService
import club.staircrusher.stdlib.di.annotation.Component
import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

@Component
class SlackServiceImpl(
    properties: SlackProperties
) : SlackService {
    private val logger = KotlinLogging.logger {}
    private val slackService = createExternalApiService<SlackService>(
        baseUrl = "https://slack.com",
        defaultHeadersBlock = { it.add(HttpHeaders.AUTHORIZATION, "Bearer ${properties.token}") },
        defaultErrorHandler = { response ->
            response
                .bodyToMono(String::class.java)
                .map { RuntimeException(it) }
                .onErrorResume { response.createException() }
        },
    )

    override fun send(channel: String, content: String) {
        try {
            val response = slackService.postMessage(
                SlackService.PostMessageRequest(
                    channel = channel,
                    text = content,
                    attachments = emptyList(),
                )
            )

            if (!response.ok) {
                logger.error("Failed to send message: $content; response: $response")
            }
        } catch (t: Throwable) {
            logger.error("Error sending message to channel $channel", t)
        }
    }

    private interface SlackService {
        @PostExchange(
            url = "/api/chat.postMessage",
            accept = ["application/json"],
            contentType = "application/json"
        )
        fun postMessage(
            @RequestBody requestBody: PostMessageRequest,
        ): PostMessageResponse

        /**
         * https://api.slack.com/methods/chat.postMessage
         */
        data class PostMessageRequest(
            val channel: String,
            val text: String,
            val attachments: List<Attachment>,
        ) {
            data class Attachment(
                val text: String,
                val actions: List<Action>,
            ) {
                data class Action(
                    val type: String = "button",
                    val text: String,
                    val url: String,
                )
            }
        }

        data class PostMessageResponse(
            val ok: Boolean,
            val channel: String,
            // Slack 메세지가 전송된 timestamp
            // channel 과 조합해서 id 처럼 쓰인다
            val ts: String,
            val error: String? = null,
        )
    }
}
