package club.staircrusher.stdlib.example

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StringUtilsTest {
    @Test
    fun `reverse - should reverse the input string`() {
        // Given
        val input = "hello"
        val expected = "olleh"

        // When
        val result = StringUtils.reverse(input)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `reverse - should handle empty string`() {
        // Given
        val input = ""
        val expected = ""

        // When
        val result = StringUtils.reverse(input)

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `isPalindrome - should return true for palindromes`() {
        // Given
        val palindromes = listOf(
            "racecar",
            "A man, a plan, a canal: Panama",
            "No 'x' in Nixon",
            ""  // Empty string is a palindrome
        )

        // When/Then
        palindromes.forEach {
            assertTrue(StringUtils.isPalindrome(it), "Failed for: $it")
        }
    }

    @Test
    fun `isPalindrome - should return false for non-palindromes`() {
        // Given
        val nonPalindromes = listOf(
            "hello",
            "world",
            "kotlin"
        )

        // When/Then
        nonPalindromes.forEach {
            assertFalse(StringUtils.isPalindrome(it), "Failed for: $it")
        }
    }
}
