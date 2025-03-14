package club.staircrusher.user.infra.adapter.out.web.login.kakao

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.kakao")
data class KakaoProperties(
    val restApiKey: String,
    val adminKey: String,
)
