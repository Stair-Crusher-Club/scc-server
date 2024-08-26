package club.staircrusher.notification.adapter.out

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.notification.push")
data class PushSenderProperties(
    val credential: String,
)
