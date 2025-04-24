package club.staircrusher.spring_web.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest

// Test enum with UNDEFINED fallback value
enum class TestEnum {
    VALUE1,
    VALUE2,
    UNDEFINED
}

enum class TestEnum2 {
    VALUE1,
    VALUE2
}

// Data class for testing
data class EnumContainer(val enumValue: TestEnum)
data class EnumContainer2(val enumValue: TestEnum2)

@SpringBootTest
@AutoConfigureMockMvc
class SccJacksonConfigTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper


    @Test
    fun `EnumFallbackModule should handle valid enum values`() {
        // Test with a valid enum value
        val json = """{"enumValue":"VALUE1"}"""
        val result = objectMapper.readValue<EnumContainer>(json)
        assertEquals(TestEnum.VALUE1, result.enumValue)
    }

    @Test
    fun `EnumFallbackModule should fallback to UNDEFINED for unknown values`() {
        // Test with an unknown enum value that should fallback to UNDEFINED
        val json = """{"enumValue":"UNKNOWN_VALUE"}"""
        val result = assertDoesNotThrow {
            objectMapper.readValue<EnumContainer>(json)
        }
        assertEquals(TestEnum.UNDEFINED, result.enumValue)
    }

    @Test
    fun `EnumFallbackModule failed with no UNDEFINED values`() {
        val json = """{"enumValue":"VALUE3"}"""
        val exception = assertThrows<MismatchedInputException> {
            val a = objectMapper.readValue<EnumContainer2>(json)
            println(a)
        }
        assert(exception.message!!.startsWith("No matching enum and no UNDEFINED fallback."))
    }
}
