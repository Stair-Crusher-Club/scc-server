package club.staircrusher.stdlib.jpa

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter

abstract class ListToTextAttributeConverter<E> : AttributeConverter<List<E>, String> {
    override fun convertToDatabaseColumn(attribute: List<E>): String {
//        return objectMapper.writeValueAsString(attribute) // FIXME: json으로 바꾸기
        return attribute.joinToString(LEGACY_DELIMITER) { convertElementToTextColumn(it) }
    }

    override fun convertToEntityAttribute(column: String): List<E> {
        return try {
            convertJsonColumnToEntityAttribute(column)
        } catch (e: JsonProcessingException) {
            convertLegacyColumnToEntityAttribute(column)
        }
    }

    private fun convertLegacyColumnToEntityAttribute(column: String): List<E> {
        return column.split(LEGACY_DELIMITER)
            .filter { it.isNotBlank() }
            .map(::convertElementFromTextColumn)
    }

    private fun convertJsonColumnToEntityAttribute(column: String): List<E> {
        return objectMapper.readValue<List<String>>(column)
            .map(::convertElementFromTextColumn)
    }

    abstract fun convertElementToTextColumn(element: E): String
    abstract fun convertElementFromTextColumn(text: String): E

    companion object {
        private val objectMapper = jacksonObjectMapper()
        const val LEGACY_DELIMITER = ",,"
    }
}
