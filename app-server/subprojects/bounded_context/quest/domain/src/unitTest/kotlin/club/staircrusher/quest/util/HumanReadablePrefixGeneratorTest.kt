package club.staircrusher.quest.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HumanReadablePrefixGeneratorTest {
    @Test
    fun test() {
        assertEquals("A", HumanReadablePrefixGenerator.generateByAlphabet(0))
        assertEquals("Z", HumanReadablePrefixGenerator.generateByAlphabet(25))
        assertEquals("AA", HumanReadablePrefixGenerator.generateByAlphabet(26))
        assertEquals("AZ", HumanReadablePrefixGenerator.generateByAlphabet(51))
        assertEquals("BA", HumanReadablePrefixGenerator.generateByAlphabet(52))
    }
}
