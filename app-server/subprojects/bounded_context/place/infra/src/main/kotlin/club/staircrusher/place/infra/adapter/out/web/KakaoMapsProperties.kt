package club.staircrusher.place.infra.adapter.out.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.kakao")
data class KakaoMapsProperties(
    val restApiKey: String,
)
