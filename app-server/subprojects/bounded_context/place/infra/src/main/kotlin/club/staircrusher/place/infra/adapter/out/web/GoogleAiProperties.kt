package club.staircrusher.place.infra.adapter.out.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.googleai")
internal data class GoogleAiProperties(
    val apiKey: String?,
)
