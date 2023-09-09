package club.staircrusher.user.infra.adapter.out.web.login.apple

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.apple-login")
data class AppleLoginProperties(
    val serviceId: String,
    val clientSecret: String,
)
