package club.staircrusher.user.application.port.out.web

// 카카오 로그인에서 제공하는 OpenID Connect ID token.
// 정확한 규격은 https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token-response-id-token 참고.
data class KakaoIdToken(
    val issuer: String, // ID 토큰을 발급한 인증 기관 정보; https://kauth.kakao.com로 고정
    val audience: String, // ID 토큰이 발급된 앱의 앱 키; 인가 코드 받기 요청 시 client_id에 전달된 앱 키
    val expiresAtEpochSecond: Long, // ID 토큰 만료 시간, UNIX 타임스탬프(Timestamp)
    val kakaoSyncUserId: String, // ID 토큰에 해당하는 사용자의 회원번호
)
