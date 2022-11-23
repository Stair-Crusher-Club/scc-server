package club.staircrusher.quest.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HumanReadablePrefixGeneratorTest {
    @Test
    fun test() {
        assertEquals("A", HumanReadablePrefixGenerator.generate(0))
        assertEquals("Z", HumanReadablePrefixGenerator.generate(25))
        assertEquals("AA", HumanReadablePrefixGenerator.generate(26))
        assertEquals("AZ", HumanReadablePrefixGenerator.generate(51))
        assertEquals("BA", HumanReadablePrefixGenerator.generate(52))
    }
}
