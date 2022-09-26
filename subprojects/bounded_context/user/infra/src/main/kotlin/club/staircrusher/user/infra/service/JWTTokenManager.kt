package club.staircrusher.user.infra.service

import club.staircrusher.user.domain.service.TokenManager
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Clock
import java.time.Duration
import java.util.Date
import kotlin.reflect.KClass

class JWTTokenManager(
    secret: String,
    private val clock: Clock,
) : TokenManager {
    private val objectMapper = jacksonObjectMapper()

    private val jwtAlgorithm = Algorithm.HMAC512(secret)
    private val bodyKey = "_b"
    private val issuer = "our-map-server"

    override fun issueToken(content: Any): String {
        return JWT.create()
            .withIssuer(issuer)
            .withClaim(bodyKey, objectMapper.writeValueAsString(content))
            .withExpiresAt(Date((clock.instant() + Duration.ofDays(30)).toEpochMilli()))
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
