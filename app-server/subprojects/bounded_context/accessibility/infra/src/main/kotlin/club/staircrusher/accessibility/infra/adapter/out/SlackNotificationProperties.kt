package club.staircrusher.accessibility.infra.adapter.out

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.slack")
data class SlackNotificationProperties(
    val token: String
)
