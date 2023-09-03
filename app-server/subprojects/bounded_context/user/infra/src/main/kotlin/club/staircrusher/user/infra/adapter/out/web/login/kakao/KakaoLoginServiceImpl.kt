package club.staircrusher.user.infra.adapter.out.web.login.kakao

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.web.login.kakao.InvalidKakaoIdTokenException
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoIdToken
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
import com.auth0.jwt.JWT

@Component
class KakaoLoginServiceImpl(
    private val kakaoLoginProperties: KakaoLoginProperties,
) : KakaoLoginService {
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
