package club.staircrusher.stdlib.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import club.staircrusher.stdlib.di.annotation.Component
import com.auth0.jwt.exceptions.JWTDecodeException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
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

    private val verifier = JWT.require(jwtAlgorithm)
        .withIssuer(issuer)
        .build()

    override fun issueToken(content: Any): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(bodyKey, objectMapper.writeValueAsString(content))
            .withExpiresAt(Date((clock.instant() + TOKEN_EXPIRATION).toEpochMilli()))
            .sign(jwtAlgorithm)
    }

    @Suppress("SwallowedException")
    override fun <T : Any> verify(token: String, contentClass: KClass<T>): T {
        val jwt = try {
            verifier.verify(token)
        } catch (e: JWTDecodeException) {
            throw TokenVerificationException(e.message ?: "")
        }
        return try {
            objectMapper.readValue(jwt.getClaim(bodyKey).asString(), contentClass.java)
        } catch (e: MismatchedInputException) {
            throw TokenVerificationException(e.message ?: "")
        }
    }
}
