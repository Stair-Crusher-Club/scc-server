package club.staircrusher.user.application.port.out.web.login.kakao

interface KakaoLoginService {
    fun parseIdToken(idToken: String): KakaoIdToken
}
