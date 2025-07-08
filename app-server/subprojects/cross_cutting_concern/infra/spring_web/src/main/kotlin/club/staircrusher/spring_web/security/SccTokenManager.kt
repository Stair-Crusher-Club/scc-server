package club.staircrusher.spring_web.security

import club.staircrusher.stdlib.clock.SccClock
import club.staircrusher.stdlib.di.annotation.Component
import club.staircrusher.stdlib.token.TokenManager
import club.staircrusher.stdlib.token.TokenVerificationException
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.AlgorithmMismatchException
import com.auth0.jwt.exceptions.InvalidClaimException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration
import kotlin.reflect.KClass

@Component
class SccTokenManager(
    sccTokenProperties: SccTokenProperties,
) : TokenManager {
    private val objectMapper = jacksonObjectMapper()

    private val jwtAlgorithm = Algorithm.HMAC512(sccTokenProperties.secret)
    private val oldJwtAlgorithm = Algorithm.HMAC512(sccTokenProperties.oldSecret)
    private val bodyKey = "_b"
    private val issuer = "our-map-server"

    private val verifier = JWT.require(jwtAlgorithm)
        .withIssuer(issuer)
        .build()
    private val oldVerifier = JWT.require(oldJwtAlgorithm)
        .withIssuer(issuer)
        .build()

    override fun issueToken(content: Any): String {
        val expiresAt = SccClock.instant() + TOKEN_EXPIRATION
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(bodyKey, objectMapper.writeValueAsString(content))
            .withExpiresAt(expiresAt)
            .sign(jwtAlgorithm)
    }

    override fun issueToken(content: Any, expiresAfter: Duration): String {
        val expiresAt = SccClock.instant() + expiresAfter
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(bodyKey, objectMapper.writeValueAsString(content))
            .withExpiresAt(expiresAt)
            .sign(jwtAlgorithm)
    }

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    override fun <T : Any> verify(token: String, contentClass: KClass<T>): T {
        // 하위 호환성
        val verificationAttempts = listOf(
            { verifier.verify(token) },
            { oldVerifier.verify(token) }
        )

        val jwt = try {
            verifyWithVerifiers(token, verificationAttempts)
        } catch (t: Throwable) {
            when (t) {
                is JWTDecodeException,
                is AlgorithmMismatchException,
                is TokenExpiredException,
                is InvalidClaimException,
                is SignatureVerificationException -> throw TokenVerificationException(t.message ?: "")
                else -> throw t
            }
        }
        return try {
            objectMapper.readValue(jwt.getClaim(bodyKey).asString(), contentClass.java)
        } catch (e: MismatchedInputException) {
            throw TokenVerificationException(e.message ?: "")
        }
    }

    private fun verifyWithVerifiers(token: String, verifiers: List<() -> DecodedJWT>): DecodedJWT {
        if (verifiers.isEmpty()) {
            throw TokenVerificationException("")
        }

        return try {
            verifiers.first().invoke()
        } catch (_: SignatureVerificationException) {
            verifyWithVerifiers(token, verifiers.drop(1))
        }
    }

    companion object {
        private val TOKEN_EXPIRATION = Duration.ofDays(365)
    }
}
