package club.staircrusher.user.infra.adapter.out.web.login.apple

import org.springframework.boot.context.properties.ConfigurationProperties

// TODO: DEV / PROD 환경의 secrets.yaml에 올바른 값 넣기
@ConfigurationProperties("scc.apple-login")
data class AppleLoginProperties(
    /**
     * The identifier (App ID or Services ID) for your app.
     * The identifier must not include your Team ID,
     * to help prevent the possibility of exposing sensitive data to the end user.
     * ref: https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens#http-body
     */
    val serviceId: String,
    /**
     * A secret JSON Web Token, generated by the developer,
     * that uses the Sign in with Apple private key associated with your developer account.
     * Authorization code and refresh token validation requests require this parameter.
     * ref: https://developer.apple.com/documentation/sign_in_with_apple/generate_and_validate_tokens#http-body
     */
    val clientSecret: String,
)
