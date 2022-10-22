package club.staircrusher.place.infra.adapter.out.web

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("scc.kakao")
data class KakaoProperties @ConstructorBinding constructor(
    val apiKey: String,
)
