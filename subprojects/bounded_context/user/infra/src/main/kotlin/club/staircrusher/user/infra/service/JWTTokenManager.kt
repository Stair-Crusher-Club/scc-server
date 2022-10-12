package club.staircrusher.user.infra.service

import club.staircrusher.user.domain.service.TokenManager
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import club.staircrusher.stdlib.di.annotation.Component
import java.time.Clock
import java.time.Duration
import java.util.Date
import kotlin.reflect.KClass

@Component
class JWTTokenManager(
    secret: String = "secret",
    private val clock: Clock,
) : TokenManager {
    companion object {
        private val TOKEN_EXPIRATION = Duration.ofDays(30)
    }
    private val objectMapper = jacksonObjectMapper()

    private val jwtAlgorithm = Algorithm.HMAC512(secret)
    private val bodyKey = "_b"
    private val issuer = "our-map-server"

    override fun issueToken(content: Any): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(bodyKey, objectMapper.writeValueAsString(content))
            .withExpiresAt(Date((clock.instant() + TOKEN_EXPIRATION).toEpochMilli()))
            .sign(jwtAlgorithm)
    }

    override fun <T : Any> verify(token: String, contentClass: KClass<T>): T {
        val verifier = JWT.require(jwtAlgorithm)
            .withIssuer(issuer)
            .build()
        val jwt = verifier.verify(token)
        return objectMapper.readValue(jwt.getClaim(bodyKey).asString(), contentClass.java)
    }
}
