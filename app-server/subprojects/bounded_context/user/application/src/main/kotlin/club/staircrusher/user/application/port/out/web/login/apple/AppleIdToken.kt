package club.staircrusher.user.application.port.out.web.login.apple

import java.time.Instant

// 애플 로그인에서 제공하는 identity token.
// 정확한 규격은 https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api/authenticating_users_with_sign_in_with_apple#3383773 참고.
data class AppleIdToken(
    val issuer: String, // 토큰을 발급한 인증 기관 정보; https://kauth.kakao.com로 고정
    val audience: String, // 토큰이 발급된 앱의 앱 키; 인가 코드 받기 요청 시 client_id에 전달된 앱 키
    private val expiresAtEpochSecond: Long, // ID 토큰 만료 시간, UNIX 타임스탬프(Timestamp)
    val appleLoginUserId: String, // 토큰에 해당하는 사용자의 회원번호
) {
    val expiresAt = Instant.ofEpochSecond(expiresAtEpochSecond)
}
