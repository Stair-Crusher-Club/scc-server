package club.staircrusher.spring_web.mock

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.web.KakaoIdToken
import club.staircrusher.user.application.port.out.web.KakaoLoginService
import org.springframework.context.annotation.Primary

@Primary
@Component
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
