package club.staircrusher.place.domain.model.accessibility

import club.staircrusher.stdlib.persistence.jpa.ListToTextAttributeConverter
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.Converter

@Converter
object AccessibilityImageListToTextAttributeConverter : ListToTextAttributeConverter<AccessibilityImageOld>() {
    override fun convertElementToTextColumn(element: AccessibilityImageOld): String {
        return objectMapper.writeValueAsString(element)
    }

    override fun convertElementFromTextColumn(text: String): AccessibilityImageOld {
        return objectMapper.readValue<AccessibilityImageOld>(text)
    }

    private val objectMapper = jacksonObjectMapper()
        .findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}
