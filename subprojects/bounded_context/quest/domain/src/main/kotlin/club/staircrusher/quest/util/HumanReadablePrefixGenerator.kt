package club.staircrusher.quest.util

@Suppress("MagicNumber")
object HumanReadablePrefixGenerator {
    fun generate(idx: Int): String {
        check(idx >= 0)
        return if (idx > 25) {
            "${generate(idx / 26 - 1)}${generateSingle(idx % 26)}"
        } else {
            generateSingle(idx).toString()
        }
    }

    private fun generateSingle(idx: Int): Char {
        check(idx in 0..25)
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ"[idx]
    }
}
