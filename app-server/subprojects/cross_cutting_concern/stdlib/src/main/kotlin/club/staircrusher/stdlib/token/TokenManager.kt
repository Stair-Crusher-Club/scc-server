package club.staircrusher.stdlib.token

import java.time.Duration
import kotlin.reflect.KClass

interface TokenManager {
    fun issueToken(content: Any): String

    fun issueToken(content: Any, expiresAfter: Duration): String

    @Throws(TokenVerificationException::class)
    fun <T : Any> verify(token: String, contentClass: KClass<T>): T
}
