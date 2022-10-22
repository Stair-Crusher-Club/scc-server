package club.staircrusher.place.infra.adapter.out.web

import club.staircrusher.stdlib.di.annotation.Component
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.kakao")
@Component
class KakaoProperties {
    var apiKey: String = ""
}
