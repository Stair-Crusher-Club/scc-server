package club.staircrusher.spring_web.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.jwt")
data class SccTokenProperties(
    val secret: String,
    // secret 을 바꾸면서 하위호환성을 위해 추가한 필드
    val oldSecret: String,
)
