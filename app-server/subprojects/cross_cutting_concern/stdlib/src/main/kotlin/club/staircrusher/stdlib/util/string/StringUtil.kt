package club.staircrusher.stdlib.util.string

import org.apache.commons.text.similarity.JaroWinklerDistance
import java.util.*

fun String.emptyToNull() = this.ifBlank { null }

fun String.isSimilarWith(pattern: String): Boolean {
    return simpleMatch(
        this.lowercase(Locale.US),
        pattern.lowercase(Locale.US).filter { it.isWhitespace().not() }
    )
}

private val distance = JaroWinklerDistance()
fun String.getSimilarityWith(other: String): Double {
    return distance.apply(this, other)
}

private fun simpleMatch(text: String, pattern: String): Boolean {
    var patternIndex = 0
    for (char in text) {
        if (patternIndex < pattern.length && pattern[patternIndex] == char) {
            patternIndex++
        }
        if (patternIndex == pattern.length) {
            return true
        }
    }
    return false
}
