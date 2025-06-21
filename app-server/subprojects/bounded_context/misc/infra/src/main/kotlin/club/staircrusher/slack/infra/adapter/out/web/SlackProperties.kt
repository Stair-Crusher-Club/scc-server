package club.staircrusher.slack.infra.adapter.out.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.slack")
data class SlackProperties(
    val token: String
)
