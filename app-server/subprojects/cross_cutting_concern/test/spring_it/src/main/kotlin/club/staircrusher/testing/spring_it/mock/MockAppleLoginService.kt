package club.staircrusher.testing.spring_it.mock

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.web.login.apple.AppleIdToken
import club.staircrusher.user.application.port.out.web.login.apple.AppleLoginService
import club.staircrusher.user.application.port.out.web.login.apple.AppleLoginTokens
import org.springframework.context.annotation.Primary
import java.time.Duration

@Primary
@Component
class MockAppleLoginService : AppleLoginService {
    override suspend fun getAppleLoginTokens(authorizationCode: String): AppleLoginTokens {
        return AppleLoginTokens(
            accessToken = "dummy",
            expiresAt = SccClock.instant() + Duration.ofHours(1),
            refreshToken = "dummy",
            idToken = AppleIdToken(
                issuer = "dummy",
                audience = "dummy",
                expiresAtEpochSecond = (SccClock.instant() + Duration.ofHours(1)).epochSecond,
                appleLoginUserId = "dummy",
            ),
        )
    }

    override suspend fun revoke(token: String): Boolean {
        return true
    }
}
