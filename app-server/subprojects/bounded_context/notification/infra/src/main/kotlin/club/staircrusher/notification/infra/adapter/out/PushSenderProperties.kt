package club.staircrusher.notification.infra.adapter.out

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.notification.push")
data class PushSenderProperties(
    val credential: String,
)
