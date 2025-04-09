package club.staircrusher.user.infra.adapter.out.web.login.kakao.client

import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.PostExchange
import reactor.core.publisher.Mono

internal interface KakaoAuthApiClient {
    // https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#refresh-token
    @Suppress("FunctionParameterNaming")
    @PostExchange(
        url = "/oauth/token",
        contentType = "application/x-www-form-urlencoded",
        accept = ["application/json"],
    )
    fun refreshToken(
        @RequestParam grant_type: String,
        @RequestParam client_id: String,
        @RequestParam refresh_token: String,
        @RequestParam(required = false) client_secret: String? = null,
    ): Mono<RefreshKakaoTokenResponseDto>

    enum class GrantType(val externalName: String) {
        AUTHORIZATION_CODE("authorization_code"),
        REFRESH_TOKEN("refresh_token"),
        ;
    }

    @Suppress("ConstructorParameterNaming")
    data class RefreshKakaoTokenResponseDto(
        val token_type: String,
        val access_token: String,
        val expires_in: Int,
        val id_token: String?,
        val refresh_token: String?,
        val refresh_token_expires_in: Int?,
    )
}
