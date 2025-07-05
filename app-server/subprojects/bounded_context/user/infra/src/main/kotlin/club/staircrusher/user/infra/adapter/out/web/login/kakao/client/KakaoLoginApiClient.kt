package club.staircrusher.user.infra.adapter.out.web.login.kakao.client

import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.PostExchange
import reactor.core.publisher.Mono

internal interface KakaoLoginApiClient {
    // https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#unlink
    @Suppress("FunctionParameterNaming")
    @PostExchange(
        url = "/v1/user/unlink",
        contentType = "application/x-www-form-urlencoded",
        accept = ["application/json"],
    )
    fun unlink(
        @RequestParam target_id_type: String,
        @RequestParam target_id: Long,
    ): Mono<UnlinkKakaoAccountResponseDto>

    enum class TargetIdType(val externalName: String) {
        USER_ID("user_id"),
        ;
    }

    data class UnlinkKakaoAccountResponseDto(
        val id: Long,
    )
}
