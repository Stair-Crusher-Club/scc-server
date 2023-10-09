package club.staircrusher.user.infra.adapter.out.web.login.apple

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.user.application.port.out.web.login.apple.AppleIdToken
import club.staircrusher.user.application.port.out.web.login.apple.AppleLoginService
import club.staircrusher.user.application.port.out.web.login.apple.AppleLoginTokens
import club.staircrusher.user.application.port.out.web.login.apple.InvalidAppleIdTokenException
import club.staircrusher.user.infra.adapter.out.web.login.apple.client.AppleLoginApiClient
import com.auth0.jwt.JWT
import kotlinx.coroutines.reactive.awaitFirst
import java.time.Duration

@Component
internal class AppleLoginServiceImpl(
    private val appleLoginProperties: AppleLoginProperties,
    private val appleLoginApiClient: AppleLoginApiClient,
) : AppleLoginService {

    override suspend fun getAppleLoginTokens(authorizationCode: String): AppleLoginTokens {
        val responseDto = appleLoginApiClient.getAppleLoginTokens(
            client_id = appleLoginProperties.serviceId,
            client_secret = appleLoginProperties.clientSecret,
            grant_type = AppleLoginApiClient.GrantType.AUTHORIZATION_CODE.externalName,
            code = authorizationCode,
            refresh_token = null,
        ).awaitFirst()

        return with(responseDto) {
            AppleLoginTokens(
                accessToken = access_token,
                expiresAt = SccClock.instant() + Duration.ofSeconds(expires_in.toLong()),
                refreshToken = refresh_token,
                idToken = parseIdToken(id_token),
            )
        }
    }

    private fun parseIdToken(idToken: String): AppleIdToken {
        val decodedJWT = JWT.decode(idToken)
        val appleIdToken = AppleIdToken(
            issuer = decodedJWT.getClaim("iss").asString(),
            audience = decodedJWT.getClaim("aud").asString(),
            expiresAtEpochSecond = decodedJWT.getClaim("exp").asLong(),
            appleLoginUserId = decodedJWT.getClaim("sub").asString(),
        )

        validateAppleIdToken(appleIdToken)

        return appleIdToken
    }

    // 검증 로직은 https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_rest_api/verifying_a_user#3383769 를 참고.
    @Suppress("ThrowsCount")
    private fun validateAppleIdToken(appleIdToken: AppleIdToken) {
        if (appleIdToken.issuer != "https://appleid.apple.com") {
            throw InvalidAppleIdTokenException("issuer does not match: ${appleIdToken.issuer}")
        }
        if (appleIdToken.audience != appleLoginProperties.serviceId) {
            throw InvalidAppleIdTokenException("audience does not match")
        }
        if (appleIdToken.expiresAt < SccClock.instant()) {
            throw InvalidAppleIdTokenException("id token is expired")
        }
    }
}
