package club.staircrusher.stdlib.token

import kotlin.reflect.KClass

interface TokenManager {
    fun issueToken(content: Any): String

    @Throws(TokenVerificationException::class)
    fun <T : Any> verify(token: String, contentClass: KClass<T>): T
}
