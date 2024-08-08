package club.staircrusher.stdlib.util.string

fun String.emptyToNull() = this.ifBlank { null }

// https://en.wikipedia.org/wiki/Levenshtein_distance#:~:text=The%20Levenshtein%20distance%20between%20two,defined%20the%20metric%20in%201965.
// string similarity by levenshtein distance considering korean
fun String.isSimilarWith(other: String, maxThreshold: Int = 3): Boolean {
    val similarity = jamoLevenshtein(this, other)
    println(similarity)
    return similarity <= maxThreshold
}

private fun Char.decomposeHangul(): List<Char>? {
    val result = mutableListOf<Char>()
    val choseongs =
        listOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    val joongseongs =
        listOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
    val jongseongs = listOf(
        null,
        'ㄱ',
        'ㄲ',
        'ㄳ',
        'ㄴ',
        'ㄵ',
        'ㄶ',
        'ㄷ',
        'ㄹ',
        'ㄺ',
        'ㄻ',
        'ㄼ',
        'ㄽ',
        'ㄾ',
        'ㄿ',
        'ㅀ',
        'ㅁ',
        'ㅂ',
        'ㅄ',
        'ㅅ',
        'ㅆ',
        'ㅇ',
        'ㅈ',
        'ㅊ',
        'ㅋ',
        'ㅌ',
        'ㅍ',
        'ㅎ'
    )
    val char = this
    val codePoint = char.code

    if (codePoint in 44032..55203) {
        val baseCode = codePoint - 44032
        val choseongIndex = baseCode / 21 / 28
        val joongseongIndex = baseCode / 28 % 21
        val jongseongIndex = baseCode % 28

        result.addAll(
            listOfNotNull(choseongs[choseongIndex], joongseongs[joongseongIndex], jongseongs[jongseongIndex]),
        )
    } else {
        return null
    }
    return result
}


private fun levenshtein(s1: String, s2: String, cost: Map<Pair<Char, Char>, Int> = emptyMap()): Int {
    if (s1.length < s2.length) {
        return levenshtein(s2, s1, cost)
    }

    if (s2.isEmpty()) {
        return s1.length
    }

    val previousRow = IntArray(s2.length + 1) { it }
    for (i in s1.indices) {
        val currentRow = IntArray(s2.length + 1)
        currentRow[0] = i + 1
        for (j in s2.indices) {
            val insertion = previousRow[j + 1] + 1
            val deletion = currentRow[j] + 1
            val substitution = previousRow[j] + if (s1[i] == s2[j]) 0 else cost.getOrDefault(s1[i] to s2[j], 1)
            currentRow[j + 1] = minOf(insertion, deletion, substitution)
        }
        previousRow.indices.forEach { previousRow[it] = currentRow[it] } // Optimized copy
    }
    return previousRow.last()
}

private fun jamoLevenshtein(s1: String, s2: String): Int {
    if (s1.length < s2.length) {
        return jamoLevenshtein(s2, s1)
    }

    if (s2.isEmpty()) {
        return s1.length
    }

    val previousRow = IntArray(s2.length + 1) { it }
    for (i in s1.indices) {
        val currentRow = IntArray(s2.length + 1)
        currentRow[0] = i + 1
        for (j in s2.indices) {
            val insertion = previousRow[j + 1] + 1
            val deletion = currentRow[j] + 1
            val substitution = previousRow[j] + getJamoCost(s1[i], s2[j])
            currentRow[j + 1] = minOf(insertion, deletion, substitution)
        }
        previousRow.indices.forEach { previousRow[it] = currentRow[it] } // Optimized copy
    }
    return previousRow.last()
}

private fun getJamoCost(c1: Char, c2: Char): Int {
    if (c1 == c2) return 0
    val jamo1 = c1.decomposeHangul()
    val jamo2 = c2.decomposeHangul()
    return if (jamo1 != null && jamo2 != null) levenshtein(jamo1.joinToString(""), jamo2.joinToString("")) / 3 else 1
}
