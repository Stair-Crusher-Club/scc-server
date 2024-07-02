package club.staircrusher.infra.persistence.sqldelight.column_adapter

import club.staircrusher.accessibility.domain.model.AccessibilityImage
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object AccessibilityImageListStringColumnAdapter : ListToTextColumnAdapter<AccessibilityImage>() {
    override fun convertElementToTextColumn(element: AccessibilityImage): String {
        return objectMapper.writeValueAsString(element)
    }

    override fun convertElementFromTextColumn(text: String): AccessibilityImage {
        return objectMapper.readValue(text)
    }

    private val objectMapper = jacksonObjectMapper()
        .findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}
