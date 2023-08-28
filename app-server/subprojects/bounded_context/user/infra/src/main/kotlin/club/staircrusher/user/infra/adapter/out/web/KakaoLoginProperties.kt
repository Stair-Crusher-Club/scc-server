package club.staircrusher.user.infra.adapter.out.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.kakao-login")
data class KakaoLoginProperties(
    val oauthClientId: String,
)
