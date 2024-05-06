package club.staircrusher.spring_web.security.admin

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.admin")
data class AdminAuthenticationProperties(
    val username: String = "admin",
    val password: String,
)
