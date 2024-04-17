package club.staircrusher.place.infra.adapter.out.web

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("scc.naver.openapi")
data class NaverOpenApiProperties (
    val clientId: String,
    val clientSecret: String,
)
