package club.staircrusher.testing.spring_it.mock

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoIdToken
import club.staircrusher.user.application.port.out.web.login.kakao.KakaoLoginService

class MockKakaoLoginService : KakaoLoginService {
    override fun parseIdToken(idToken: String): KakaoIdToken {
        return KakaoIdToken(
            issuer = "dummy",
            audience = "dummy",
            expiresAtEpochSecond = SccClock.instant().epochSecond,
            kakaoSyncUserId = "dummy",
        )
    }

    override suspend fun disconnect(externalId: String): Boolean {
        return true
    }
}
