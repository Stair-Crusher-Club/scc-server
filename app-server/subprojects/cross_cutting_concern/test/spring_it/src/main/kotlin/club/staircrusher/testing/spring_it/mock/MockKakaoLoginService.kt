package club.staircrusher.testing.spring_it.mock

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoIdToken
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginTokens

class MockKakaoLoginService : KakaoLoginService {
    override fun parseIdToken(idToken: String): KakaoIdToken {
        return KakaoIdToken(
            issuer = "dummy",
            audience = "dummy",
            expiresAtEpochSecond = SccClock.instant().epochSecond,
            kakaoSyncUserId = "dummy",
        )
    }

    override suspend fun refreshToken(refreshToken: String): KakaoLoginTokens {
        return KakaoLoginTokens(
            accessToken = "dummy",
            accessTokenExpiresAt = SccClock.instant(),
            idToken = null,
            refreshToken = null,
            refreshTokenExpiresAt = null,
        )
    }

    override suspend fun disconnect(kakaoSyncUserId: String): Boolean {
        return true
    }
}
