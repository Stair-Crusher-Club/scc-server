package club.staircrusher.place.infra.adapter.out.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.openai")
internal data class OpenAIProperties(
    val apiKey: String?,
)
