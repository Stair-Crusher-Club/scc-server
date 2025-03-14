package club.staircrusher.user.infra.adapter.out.web.login.kakao

import club.staircrusher.infra.network.createExternalApiService
import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.web.login.kakao.InvalidKakaoIdTokenException
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoIdToken
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginTokens
import club.staircrusher.user.infra.adapter.out.web.login.kakao.client.KakaoAuthApiClient
import club.staircrusher.user.infra.adapter.out.web.login.kakao.client.KakaoLoginApiClient
import com.auth0.jwt.JWT
import kotlinx.coroutines.reactive.awaitFirst
import mu.KotlinLogging
import org.springframework.http.HttpHeaders

@Component
class KakaoLoginServiceImpl(
    private val kakaoProperties: KakaoProperties,
    private val kakaoLoginProperties: KakaoLoginProperties,
) : KakaoLoginService {
    private val logger = KotlinLogging.logger {}

    private val kakaoLoginApiClient = createExternalApiService<KakaoLoginApiClient>(
        baseUrl = "https://kapi.kakao.com",
        // TODO: admin key 사용하지 않기
        defaultHeadersBlock = { it.add(HttpHeaders.AUTHORIZATION, "KakaoAK ${kakaoProperties.adminKey}") },
        defaultErrorHandler = { response ->
            response
                .bodyToMono(String::class.java)
                .map { RuntimeException(it) }
                .onErrorResume { response.createException() }
        }
    )
    private val kakaoAuthApiClient = createExternalApiService<KakaoAuthApiClient>(
        baseUrl = "https://kauth.kakao.com",
        defaultHeadersBlock = {},
        defaultErrorHandler = { response ->
            response
                .bodyToMono(String::class.java)
                .map { RuntimeException(it) }
                .onErrorResume { response.createException() }
        }
    )

    override fun parseIdToken(idToken: String): KakaoIdToken {
        // TODO: 정말 카카오 서버에서 sign한 토큰이 맞는지 검증 필요
        val decodedJWT = JWT.decode(idToken)
        val kakaoIdToken = KakaoIdToken(
            issuer = decodedJWT.getClaim("iss").asString(),
            audience = decodedJWT.getClaim("aud").asString(),
            expiresAtEpochSecond = decodedJWT.getClaim("exp").asLong(),
            kakaoSyncUserId = decodedJWT.getClaim("sub").asString(),
        )

        validateKakaoIdToken(kakaoIdToken)

        return kakaoIdToken
    }

    override suspend fun refreshToken(refreshToken: String): KakaoLoginTokens? {
        return try {
            val response = kakaoAuthApiClient.refreshToken(
                grant_type = KakaoAuthApiClient.GrantType.REFRESH_TOKEN.externalName,
                client_id = kakaoProperties.restApiKey,
                refresh_token = refreshToken,
            ).awaitFirst()

            with(response) {
                KakaoLoginTokens(
                    accessToken = access_token,
                    accessTokenExpiresAt = SccClock.instant().plusSeconds(expires_in.toLong()),
                    idToken = id_token?.let { parseIdToken(it) },
                    refreshToken = refresh_token,
                    refreshTokenExpiresAt = refresh_token_expires_in?.let { SccClock.instant().plusSeconds(it.toLong()) },
                )
            }

        } catch (t: Throwable) {
            logger.error(t) { "Failed to refresh kakao token" }
            null
        }
    }

    override suspend fun disconnect(kakaoSyncUserId: String): Boolean {
        val syncId = kakaoSyncUserId.toLongOrNull() ?: return false
        val response = kakaoLoginApiClient.unlink(
            target_id_type = KakaoLoginApiClient.TargetIdType.USER_ID.externalName,
            target_id = syncId,
        ).awaitFirst()

        return response.id == syncId
    }

    // 검증 로직은 https://developers.kakao.com/docs/latest/ko/kakaologin/common#oidc 를 참고.
    @Suppress("ThrowsCount")
    private fun validateKakaoIdToken(kakaoIdToken: KakaoIdToken) {
        if (kakaoIdToken.issuer != "https://kauth.kakao.com") {
            throw InvalidKakaoIdTokenException("issuer does not match: ${kakaoIdToken.issuer}")
        }
        if (kakaoIdToken.audience != kakaoLoginProperties.oauthClientId) {
            throw InvalidKakaoIdTokenException("audience does not match")
        }
        if (kakaoIdToken.expiresAt < SccClock.instant()) {
            throw InvalidKakaoIdTokenException("id token is expired")
        }
    }
}
