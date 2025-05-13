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
        val jwt = try {
            verifier.verify(token)
        } catch (_: SignatureVerificationException) {
            // 하위 호환성 맞춰주기
            try {
                oldVerifier.verify(token)
            } catch (e: SignatureVerificationException) {
                throw TokenVerificationException(e.message ?: "")
            }
        } catch (t: Throwable) {
            when (t) {
                is JWTDecodeException,
                is AlgorithmMismatchException,
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

    companion object {
        private val TOKEN_EXPIRATION = Duration.ofDays(365)
    }
}
