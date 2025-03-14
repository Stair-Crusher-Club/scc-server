package club.staircrusher.user.application.port.out.web.login.kakao

import java.time.Instant

data class KakaoLoginTokens(
    val accessToken: String,
    val accessTokenExpiresAt: Instant,
    val idToken: KakaoIdToken?,
    val refreshToken: String?,
    val refreshTokenExpiresAt: Instant?,
)
