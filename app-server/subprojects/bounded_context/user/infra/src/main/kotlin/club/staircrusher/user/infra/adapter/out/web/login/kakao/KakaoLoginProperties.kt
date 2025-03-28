package club.staircrusher.user.infra.adapter.out.web.login.kakao

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.kakao-login")
data class KakaoLoginProperties(
    val oauthClientId: String,
    val adminKey: String,
)
