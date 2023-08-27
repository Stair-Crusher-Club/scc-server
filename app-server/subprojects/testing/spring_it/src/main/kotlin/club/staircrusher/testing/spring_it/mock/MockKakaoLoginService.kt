package club.staircrusher.testing.spring_it.mock

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.user.application.port.out.web.KakaoIdToken
import club.staircrusher.user.application.port.out.web.KakaoLoginService

class MockKakaoLoginService : KakaoLoginService {
    override fun parseIdToken(idToken: String): KakaoIdToken {
        return KakaoIdToken(
            issuer = "dummy",
            audience = "dummy",
            expiresAtEpochSecond = SccClock.instant().epochSecond,
            kakaoSyncUserId = "dummy",
        )
    }
}
