package club.staircrusher.place.infra.adapter.out.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.open-data")
data class GovernmentOpenDataProperties(
    val apiKey: String,
)
