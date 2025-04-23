package club.staircrusher.stdlib.example

/**
 * Utility functions for string manipulation.
 */
object StringUtils {
    /**
     * Reverses a string.
     *
     * @param input The string to reverse
     * @return The reversed string
     */
    fun reverse(input: String): String {
        return input.reversed()
    }

    /**
     * Checks if a string is a palindrome (reads the same forward and backward).
     *
     * @param input The string to check
     * @return True if the string is a palindrome, false otherwise
     */
    fun isPalindrome(input: String): Boolean {
        val cleaned = input.lowercase().replace(Regex("[^a-z0-9]"), "")
        return cleaned == cleaned.reversed()
    }
}
