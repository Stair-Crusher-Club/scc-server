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

// Test enum with UNKNOWN fallback value
enum class TestEnum {
    VALUE1,
    VALUE2,
    UNKNOWN
}

enum class TestEnum2 {
    VALUE1,
    VALUE2
}

// Data class for testing
data class EnumContainer(val enumValue: TestEnum?)
data class EnumContainer2(val enumValue: TestEnum2)

@SpringBootTest
class SccJacksonConfigTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper


    @Test
    fun `EnumFallbackModule should handle valid enum values`() {
        val json = """{"enumValue":"VALUE1"}"""
        val result = objectMapper.readValue<EnumContainer>(json)
        assertEquals(TestEnum.VALUE1, result.enumValue)
    }

    @Test
    fun `EnumFallbackModule should fallback to UNKNOWN for unknown values`() {
        val json = """{"enumValue":"UNKNOWN_VALUE"}"""
        val result = assertDoesNotThrow {
            objectMapper.readValue<EnumContainer>(json)
        }
        assertEquals(TestEnum.UNKNOWN, result.enumValue)
    }

    @Test
    fun `EnumFallbackModule should handle null case`() {
        val json = """{"enumValue":null}"""
        val result = assertDoesNotThrow {
            objectMapper.readValue<EnumContainer>(json)
        }
        assertEquals(null, result.enumValue)
    }

    @Test
    fun `EnumFallbackModule should handle empty case`() {
        val json = """{}"""
        val result = assertDoesNotThrow {
            objectMapper.readValue<EnumContainer>(json)
        }
        assertEquals(null, result.enumValue)
    }

    @Test
    fun `EnumFallbackModule failed with no UNKNOWN values`() {
        val json = """{"enumValue":"VALUE3"}"""
        val exception = assertThrows<MismatchedInputException> {
            val a = objectMapper.readValue<EnumContainer2>(json)
            println(a)
        }
        assert(exception.message!!.startsWith("No matching enum and no UNKNOWN fallback."))
    }
}
