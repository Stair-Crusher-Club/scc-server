package club.staircrusher.stdlib.util.string

import java.util.*

fun String.emptyToNull() = this.ifBlank { null }

fun String.isSimilarWith(pattern: String): Boolean {
    return simpleMatch(this.lowercase(Locale.US), pattern.lowercase(Locale.US))
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
