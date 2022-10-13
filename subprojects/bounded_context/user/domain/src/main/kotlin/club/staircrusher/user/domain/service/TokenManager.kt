package club.staircrusher.user.domain.service

import club.staircrusher.user.domain.exception.TokenVerificationException
import kotlin.reflect.KClass

interface TokenManager {
    fun issueToken(content: Any): String

    @Throws(TokenVerificationException::class)
    fun <T : Any> verify(token: String, contentClass: KClass<T>): T
}
