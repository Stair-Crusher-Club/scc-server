package club.staircrusher.user.infra.adapter.out.web.login.apple.client

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.PostExchange
import reactor.core.publisher.Mono

internal interface AppleLoginApiClient {
    // https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens
    @Suppress("FunctionParameterNaming")
    @PostExchange(
        url = "/auth/token",
        contentType = "application/x-www-form-urlencoded",
        accept = ["application/json"],
    )
    fun getAppleLoginTokens(
        @RequestParam client_id: String,
        @RequestParam client_secret: String,
        @RequestParam grant_type: String,
        @RequestParam(required = false) code: String?,
        @RequestParam(required = false) refresh_token: String?,
        @RequestParam(required = false) redirect_uri: String? = null,
    ): Mono<GetAppleLoginTokensResponseDto>

    // https://developer.apple.com/documentation/signinwithapplerestapi/revoke_tokens
    @Suppress("FunctionParameterNaming")
    @PostExchange(
        url = "/auth/revoke",
        contentType = "application/x-www-form-urlencoded",
        accept = ["application/json"],
    )
    fun revoke(
        @RequestParam client_id: String,
        @RequestParam client_secret: String,
        @RequestParam token: String,
        @RequestParam(required = false) token_type_hint: String,
    ): Mono<ResponseEntity<Unit>>

    enum class GrantType(val externalName: String) {
        AUTHORIZATION_CODE("authorization_code"),
        REFRESH_TOKEN("refresh_token"),
        ;
    }

    enum class TokenType(val externalName: String) {
        ACCESS_TOKEN("access_token"),
        REFRESH_TOKEN("refresh_token"),
        ;
    }

    @Suppress("ConstructorParameterNaming")
    data class GetAppleLoginTokensResponseDto(
        val access_token: String,
        val token_type: String,
        val expires_in: Int,
        val refresh_token: String,
        val id_token: String,
    )
}
