package club.staircrusher.stdlib.testing

import java.time.Instant
import java.util.Base64
import kotlin.random.Random

object SccRandom {
    private val random = Random(Instant.now().toEpochMilli())

    fun string(length: Int): String {
        return Base64.getEncoder().encodeToString(random.nextBytes(length)).take(length)
    }
}
