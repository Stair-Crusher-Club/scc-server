package club.staircrusher.stdlib.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import club.staircrusher.stdlib.di.annotation.Component
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.InvalidClaimException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
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
        private val TOKEN_EXPIRATION = Duration.ofDays(365)
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

    override fun issueToken(content: Any, expiresAfter: Duration): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(bodyKey, objectMapper.writeValueAsString(content))
            .withExpiresAt(Date((clock.instant() + expiresAfter).toEpochMilli()))
            .sign(jwtAlgorithm)
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override fun <T : Any> verify(token: String, contentClass: KClass<T>): T {
        val jwt = try {
            verifier.verify(token)
        } catch (t: Throwable) {
            when (t) {
                is JWTDecodeException,
                is AlgorithmMismatchException,
                is SignatureVerificationException,
                is TokenExpiredException,
                is InvalidClaimException -> throw TokenVerificationException(t.message ?: "")
                else -> throw t
            }
        }
        return try {
            objectMapper.readValue(jwt.getClaim(bodyKey).asString(), contentClass.java)
        } catch (e: MismatchedInputException) {
            throw TokenVerificationException(e.message ?: "")
        }
    }
}
