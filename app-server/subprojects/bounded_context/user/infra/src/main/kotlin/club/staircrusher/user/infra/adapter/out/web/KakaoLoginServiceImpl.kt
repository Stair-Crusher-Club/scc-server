package club.staircrusher.user.infra.adapter.out.web

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.web.InvalidKakaoIdTokenException
import club.staircrusher.user.application.port.out.web.KakaoIdToken
import club.staircrusher.user.application.port.out.web.KakaoLoginService
import com.auth0.jwt.JWT

@Component
class KakaoLoginServiceImpl(
    private val kakaoLoginProperties: KakaoLoginProperties,
) : KakaoLoginService {
    override fun parseIdToken(idToken: String): KakaoIdToken {
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
