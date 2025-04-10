package club.staircrusher.user.application.port.out.web.login.apple

interface AppleLoginService {
    suspend fun getAppleLoginTokens(authorizationCode: String): AppleLoginTokens
    suspend fun revoke(token: String): Boolean
}
