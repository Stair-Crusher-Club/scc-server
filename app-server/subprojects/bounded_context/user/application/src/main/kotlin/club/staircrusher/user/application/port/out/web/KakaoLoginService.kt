package club.staircrusher.user.application.port.out.web

interface KakaoLoginService {
    fun parseIdToken(idToken: String): KakaoIdToken
}
