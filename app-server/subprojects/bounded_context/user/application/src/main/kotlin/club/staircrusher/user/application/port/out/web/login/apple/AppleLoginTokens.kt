package club.staircrusher.user.application.port.out.web.login.apple

import java.time.Instant

// https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens
data class AppleLoginTokens(
    val accessToken: String,
    val expiresAt: Instant,
    val refreshToken: String,
    val idToken: AppleIdToken,
)
