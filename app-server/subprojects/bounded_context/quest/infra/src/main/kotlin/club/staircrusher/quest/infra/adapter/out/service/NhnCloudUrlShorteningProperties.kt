package club.staircrusher.quest.infra.adapter.out.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.nhn-cloud.url-shortening")
data class NhnCloudUrlShorteningProperties(
    val appKey: String,
)
